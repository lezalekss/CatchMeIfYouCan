package com.rmt.services;

import com.rmt.domain.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.rmt.domain.Question;
import javafx.collections.ObservableSet;

import static com.rmt.domain.Message.MessageType.*;

public class CommunicationService {

    private static CommunicationService serviceInstance = null;
    private Socket communicationSocket = null;
    private ObjectInputStream serverInput = null;
    private ObjectOutputStream serverOutput = null;

    private static final ReentrantReadWriteLock serverInputLock = new ReentrantReadWriteLock(true);


    private CommunicationService() {
    }

    public static CommunicationService getCommunicationServiceInstance() {
        if (serviceInstance == null) {
            serviceInstance = new CommunicationService();
        }
        return serviceInstance;
    }

    public Question[] loadQuickQuestions() {
        try {
            this.sendMessage(GET_QUICK_QUESTIONS, "");
            Question[] quickQuestions = (Question[]) this.serverInput.readObject();
            return quickQuestions;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean connect() {
        try {
            this.communicationSocket = new Socket("localhost", 5000);
            this.serverInput = new ObjectInputStream(this.communicationSocket.getInputStream());
            this.serverOutput = new ObjectOutputStream(this.communicationSocket.getOutputStream());
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e1) {
            return false;
        }
        return true;
    }

    public boolean register(String username, String password) throws IOException {
//      #TODO encript password before sending
        this.sendMessage(Message.MessageType.REGISTER, username + "#" + password);
        try {
            Message answer = (Message) this.serverInput.readObject();
            switch (answer.getType()) {
                case ANSWERS: {
                    return true;
                }
                case ERROR: {
                    return false;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String login(String username, String password) throws IOException {
//      #TODO check whether username contains : or # before sending
        try {
            this.sendMessage(Message.MessageType.LOGIN, username + "#" + password);
            Message answer = (Message) this.serverInput.readObject();
            return answer.getMessageText();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(Message.MessageType messageType, String messageText) throws IOException {
        Message message = new Message(messageType, messageText);
        this.serverOutput.writeObject(message);
    }

    public Set<String> getActivePlayers() {
        try {
            this.sendMessage(Message.MessageType.GET_ACTIVE, "");
            return (Set<String>) this.serverInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean challengeOpponent(String opponentUsername) {
        try {
            this.sendMessage(Message.MessageType.PLAY_WITH, opponentUsername);

            Message answer = (Message) this.serverInput.readObject();

            if (answer.getType() == Message.MessageType.ANSWERS && answer.getMessageText().equals("YES")) {
                this.sendMessage(GAME_ACCEPTED, opponentUsername);
                System.out.println("Game accepted message sent from challengeOpponent");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //change
        return false;
    }

    public void updatePlayers(ObservableSet<String> players) {
        try {
            serverInputLock.readLock().lock();
            try {
                this.serverOutput.writeObject(new Message(Message.MessageType.UPDATE_ACTIVE, ""));
                HashSet<String> updatedPlayers = (HashSet<String>) this.serverInput.readObject();

                players.removeAll(players);
                players.addAll(updatedPlayers);
            } finally {
                serverInputLock.readLock().unlock();
            }
        } catch (ClassNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void challengeAccepted(String challengerUsername) {
        try {
            this.sendMessage(Message.MessageType.CHALLENGE_ANSWER, "YES\n" + challengerUsername);
            this.serverOutput.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void challengeRejected(String challengerUsername) {
        try {
            this.sendMessage(Message.MessageType.CHALLENGE_ANSWER, "NO\n" + challengerUsername);
            this.serverOutput.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            this.sendMessage(Message.MessageType.LOG_OUT, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tellServerToSwitchToWaiting() {
        try {
            this.sendMessage(Message.MessageType.SWITCH, "TO WAITING");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tellServerToSwitchToChallenging() {
        try {
            this.sendMessage(Message.MessageType.SWITCH, "TO CHALLENGING");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWaitingTask(WaitingTask waitingTask) {
        waitingTask.setServerInput(this.serverInput);
        Thread thread = new Thread(waitingTask);
//        testiraj kroz debugger
        thread.setDaemon(true);
        thread.start();
    }

    public void tellServerToStartGame(String challengedUsername) {
        try {
            this.sendMessage(GAME_ACCEPTED, challengedUsername);
            System.out.println("Game accepted message sent from tell server to...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAnswers(int numberOfCorrectAnswers) {
        try {
            this.sendMessage(SET_CORRECT_ANSWERS, numberOfCorrectAnswers + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Question[] loadChaseQuestions() {
        try {
            this.sendMessage(GET_CHASE_QUESTIONS, "");
            return (Question[]) this.serverInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public boolean sendChaseAnswer(boolean isAnswerCorrect) {
//        try {
//            this.sendMessage(EXCHANGE_ANSWERS, isAnswerCorrect+"");
//            boolean isOpponentCorrect;
//            Message serverAnswer = (Message) this.serverInput.readObject();
//            if(serverAnswer.getMessageText().equals("true")){
//                isOpponentCorrect = true;
//            }else {
//                isOpponentCorrect = false;
//            }
//            return isOpponentCorrect;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public void sendChaseAnswer(boolean isAnswerCorrect) {
        try {
            this.sendMessage(EXCHANGE_ANSWERS, isAnswerCorrect + "");
//            boolean isOpponentCorrect;
//            Message serverAnswer = (Message) this.serverInput.readObject();
//            if(serverAnswer.getMessageText().equals("true")){
//                isOpponentCorrect = true;
//            }else {
//                isOpponentCorrect = false;
//            }
//            return isOpponentCorrect;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gameFinished() {
        try {
            this.sendMessage(GAME_FINISHED, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message waitForMessage() {
        try {
            Message message = (Message) this.serverInput.readObject();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

