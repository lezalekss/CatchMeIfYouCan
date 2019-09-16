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

    public Question[] loadQuickQuestions() throws IOException {
        try {
            this.sendMessage(GET_QUICK_QUESTIONS, "");
            Question[] quickQuestions = (Question[]) this.serverInput.readObject();
            return quickQuestions;
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

    public Set<String> getActivePlayers() throws IOException {
        try {
            this.sendMessage(Message.MessageType.GET_ACTIVE, "");
            return (Set<String>) this.serverInput.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean challengeOpponent(String opponentUsername) throws IOException {
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //change
        return false;
    }

    public void updatePlayers(ObservableSet<String> players) throws IOException {
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

        }
    }

    public void challengeAccepted(String challengerUsername) throws IOException {
        this.sendMessage(Message.MessageType.CHALLENGE_ANSWER, "YES\n" + challengerUsername);
        this.serverOutput.flush();
    }

    public void challengeRejected(String challengerUsername) throws IOException {
        this.sendMessage(Message.MessageType.CHALLENGE_ANSWER, "NO\n" + challengerUsername);
        this.serverOutput.flush();
    }

    public void logout() throws IOException {
        this.sendMessage(Message.MessageType.LOG_OUT, "");
    }

    public void tellServerToSwitchToWaiting() throws IOException {
        this.sendMessage(Message.MessageType.SWITCH, "TO WAITING");
    }

    public void tellServerToSwitchToChallenging() throws IOException {
        this.sendMessage(Message.MessageType.SWITCH, "TO CHALLENGING");
    }

    public void startWaitingTask(WaitingTask waitingTask) {
        waitingTask.setServerInput(this.serverInput);
        Thread thread = new Thread(waitingTask);
//        testiraj kroz debugger
        thread.setDaemon(true);
        thread.start();
    }

    public void sendAnswers(int numberOfCorrectAnswers) throws IOException {
        this.sendMessage(SET_CORRECT_ANSWERS, numberOfCorrectAnswers + "");
    }

    public Question[] loadChaseQuestions() throws IOException {
        try {
            this.sendMessage(GET_CHASE_QUESTIONS, "");
            return (Question[]) this.serverInput.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendChaseAnswer(boolean isAnswerCorrect) throws IOException {
        this.sendMessage(EXCHANGE_ANSWERS, isAnswerCorrect + "");
    }

    public void gameFinished() throws IOException {
        this.sendMessage(GAME_FINISHED, "");
    }

    public Message waitForMessage() throws IOException {
        try {
            Message message = (Message) this.serverInput.readObject();
            return message;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

