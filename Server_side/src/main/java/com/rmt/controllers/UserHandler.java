package com.rmt.controllers;

import com.rmt.DBConnection;
import com.rmt.domain.Message;
import com.rmt.ServerAppMain;
import com.rmt.domain.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserHandler extends Thread {

    private Player user;
    private Player opponent;

    private ObjectInputStream userInput;
    private ObjectOutputStream userOutput;

    private ObjectInputStream opponentInput;
    private ObjectOutputStream opponentOutput;
    private DBConnection dbConn;

    private static final ReentrantReadWriteLock userOutputStreamLock = new ReentrantReadWriteLock(true);
    private static final ReentrantReadWriteLock opponentOutputStreamLock = new ReentrantReadWriteLock(true);

    public UserHandler(Socket socket) {
        user = new Player(socket);
    }

    public void run() {
        try {
            dbConn = new DBConnection();
            userOutput = new ObjectOutputStream(this.user.getSocket().getOutputStream());
            userInput = new ObjectInputStream(this.user.getSocket().getInputStream());

            user.setUserOutput(this.userOutput);
            user.setUserInput(this.userInput);

            while (true) {
                Message msg = (Message) userInput.readObject();

                switch (msg.getType()) {
                    case LOGIN:
                        this.login(msg);
                        break;
                    case REGISTER:
                        this.register(msg);
                        break;
                    case GET_ACTIVE: {
                        this.sendActivePlayers();
                        break;
                    }
                    case UPDATE_ACTIVE: {
                        this.updateActivePlayers();
                        break;
                    }
                    case PLAY_WITH: {
                        System.out.println("UH: PW primljen\n");
                        ServerAppMain.removePlayerFromActive(this.user.getUsername());
                        this.challenge(msg.getMessageText());
                        break;
                    }
                    case CHALLENGE_ANSWER: {
                        System.out.println("UH od izazvanog primio odg\n");

                        this.userOutput.writeObject(new Message(Message.MessageType.ANSWERS, "Send again"));

                        System.out.println("UH od izazvanog poslo klijentu da posalje opet odg\n");

                        if (msg.getMessageText().equals("YES")) {
                            this.startGame();
                            break;
                        } else {
                            break;
                        }
                    }case LOG_OUT:{
                        System.out.println("UH received logout message");
                        this.userOutput.writeObject(new Message(Message.MessageType.PLAY_WITH, user.getUsername()));
                        this.userOutput.flush();
                        System.out.println("UH sent test play with message");
                        break;
                    }
                    default:
                        this.sendError("Unexpected error");
                        break;
                }
            }
        } catch (java.io.EOFException e) {
            //kad kliknem na x u klijentu iskoci ovo
            ServerAppMain.removePlayerFromActive(user.getUsername());
            System.out.println("Player " + user.getUsername() + " just exited");
        } catch (SocketException e) {
            //e.printStackTrace(); // client shuts down
            ServerAppMain.removePlayerFromActive(this.user.getUsername());
            System.out.println("Player " + user.getUsername() + " just exited");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
    }

    private void challenge(String opponentUsername) throws IOException, ClassNotFoundException {
//      check for opponent status first

        if (ServerAppMain.isActive(opponentUsername) == false) {
            this.sendError("Opponent is not active anymore");
            return;
        }
        Player opponent = ServerAppMain.findPlayer(opponentUsername);

        System.out.println("Naso protivnika u mapi\n");

        //ako ga izazivam menjam mu status da ga ne izazove neko drugi, ako odbije da mu vratim status
        ServerAppMain.removePlayerFromActive(opponentUsername);

        System.out.println("UH stavio izvanog u neaktivne");

        ObjectOutputStream opponentOutput = opponent.getUserOutput();
        ObjectInputStream opponentInput = opponent.getUserInput();

        System.out.println("UH: naso za izazvanog strimove");

        opponentOutput.writeObject(new Message(Message.MessageType.PLAY_WITH, user.getUsername()));
        opponentOutput.flush();

        System.out.println("UH poslo izvanom pw\n");

        Message answer = (Message) opponentInput.readObject();

        System.out.println("UH: Izazivac primio odg\n");

        if (answer.getType() == Message.MessageType.CHALLENGE_ANSWER) {
            opponentOutput.writeObject(new Message(Message.MessageType.ANSWERS, "Send again"));
            System.out.println("UH: Izazivac poslo klijentu da posalje opet odg\n");
        }
        if (answer.getMessageText().equals("YES")) {

        } else {

        }
        //
        //
        //
        //
    }


    private void sendActivePlayers() throws IOException, ClassNotFoundException {
        HashSet<String> players = ServerAppMain.getOnlinePlayers();
        players.remove(user.getUsername());
        this.userOutput.writeObject(players);
        //TODO: Add some sleeping maybe
        ServerAppMain.addToActivePlayers(user);
    }


    private void updateActivePlayers() throws IOException {
        //da ga neko ne bi izazvao dok se updatuje
        ServerAppMain.removePlayerFromActive(this.user.getUsername());

        HashSet<String> players = ServerAppMain.getOnlinePlayers();
        players.remove(user.getUsername());

//        userOutputStreamLock.writeLock().lock();
//        try {
        this.userOutput.writeObject(players);
        this.userOutput.flush();

        System.out.println("Players sent\n");

       /* try {
            this.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        ServerAppMain.addToActivePlayers(this.user);

//        } finally {
//            userOutputStreamLock.writeLock().unlock();
        //kad se zavrsi update vrati ga u aktivne
//        }
    }

    private void login(Message msg) throws IOException {
        String[] userAndPass = msg.getMessageText().split("#");
        if (dbConn.isRegistered(userAndPass[0]) == false) {
            this.sendError("Username does not exist.");
            return;
        } else if (dbConn.isPasswordCorrect(userAndPass[0], userAndPass[1]) == false) {
            this.sendError("Incorrect password.");
            return;
        }
        this.sendAnswer("OK");
        user.setUsername(userAndPass[0]);
        user.setStatus(Player.PlayerStatus.ACTIVE);
    }

    private void register(Message msg) throws IOException {
        String[] userAndPass = msg.getMessageText().split("#");
        if (dbConn.isRegistered(userAndPass[0])) {
            this.sendError("Username already taken.");
            return;
        }
        this.sendAnswer("OK");
//        dbConn.insertIntoDatabase(userAndPass[0], userAndPass[1]);
        user.setUsername(userAndPass[0]);
        user.setStatus(Player.PlayerStatus.ACTIVE);
    }

    private void inGameScene(String username) {
//        List<String> onlinePlayers = ServerAppMain.getOnlinePlayers().stream().map(Player::getUsername).collect(Collectors.toList());
//        user.setStatus(Player.PlayerStatus.ACTIVE);
//        user.setUsername(username);
//        try {
//            userOutput.writeObject(onlinePlayers);
//            Message msg = (Message)userInput.readObject(); // U OVOJ PORUCI CE BITI USER SA KOJIM HOCE DA SE POVEZE
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private void sendError(String messageText) throws IOException {
        Message errorMessage = new Message(Message.MessageType.ERROR, messageText);
        this.userOutput.writeObject(errorMessage);
    }

    private void sendAnswer(String messageText) throws IOException {
        Message answer = new Message(Message.MessageType.ANSWERS, messageText);
        this.userOutput.writeObject(answer);
    }

    private void sendMessage(Message.MessageType type, String text) throws IOException {
        Message message = new Message(type, text);
        this.userOutput.writeObject(message);
    }

}