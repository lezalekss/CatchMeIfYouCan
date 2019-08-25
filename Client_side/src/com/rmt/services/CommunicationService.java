package com.rmt.services;

import static com.rmt.domain.Message.MessageType.GAME_ACCEPTED;

import com.rmt.domain.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.rmt.domain.Question;
import javafx.collections.ObservableSet;

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

        return new Question[0];
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

    public boolean register(String username, String password) {
//      #TODO encript password before sending
        try {
            this.sendMessage(Message.MessageType.REGISTER, username + "#" + password);
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
            return false;
        } catch (IOException e1) {
            return false;
        }
        return false;
    }

    public String login(String username, String password) {
//      #TODO check whether username contains : or # before sending
        try {
            this.sendMessage(Message.MessageType.LOGIN, username + "#" + password);
            Message answer = (Message) this.serverInput.readObject();
            return answer.getMessageText();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        System.out.println("Comm service pozvan\n");

        try {
            this.sendMessage(Message.MessageType.PLAY_WITH, opponentUsername);

            System.out.println("Poslat play with uh-u \n");

            Message answer = (Message) this.serverInput.readObject();

            System.out.println("CS izazivaca primio odg\n");

            if (answer.getType() == Message.MessageType.ANSWERS && answer.getMessageText().equals("YES")) {
                this.sendMessage(GAME_ACCEPTED,opponentUsername);
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
            System.out.println("UT locked server input stream\n");
            try {
                this.serverOutput.writeObject(new Message(Message.MessageType.UPDATE_ACTIVE, ""));
                System.out.println("UT sent request\n");
                HashSet<String> updatedPlayers = (HashSet<String>) this.serverInput.readObject();

                System.out.println("UT got the players\n");

                players.removeAll(players);
                players.addAll(updatedPlayers);
            } finally {
                serverInputLock.readLock().unlock();
                System.out.println("UT unlocked server input stream\n");
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

            System.out.println("COMM S sent YES\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void challengeRejected(String challengerUsername) {
        try {
            this.sendMessage(Message.MessageType.CHALLENGE_ANSWER, "NO\n" + challengerUsername);
            this.serverOutput.flush();

            System.out.println("COMM S sent NO\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void logout() {
        try {
            this.sendMessage(Message.MessageType.LOG_OUT, "");
            System.out.println("CS sent logut message");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToWaitingForChallenge() {
        try {
            this.sendMessage(Message.MessageType.SWITCH, "TO WAITING");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToChallenging() {
        try {
            this.sendMessage(Message.MessageType.SWITCH, "TO CHALLENGING");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitToBeChallenged(WaitingTask waitingTask) {
        waitingTask.setServerInput(this.serverInput);
        Thread thread = new Thread(waitingTask);
        thread.start();
    }
}

