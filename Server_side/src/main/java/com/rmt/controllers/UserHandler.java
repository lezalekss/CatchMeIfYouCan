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

public class UserHandler extends Thread {

    private Player user;
    private Player opponent;

    private ObjectInputStream userInput;
    private ObjectOutputStream userOutput;

    private ObjectInputStream opponentInput;
    private ObjectOutputStream opponentOutput;
    private DBConnection dbConn;

    public UserHandler(Socket socket) {
        user = new Player(socket);
    }

    public void run() {
        try {
            dbConn = new DBConnection();
            userOutput = new ObjectOutputStream(this.user.getSocket().getOutputStream());
            userInput = new ObjectInputStream(this.user.getSocket().getInputStream());

            while (true) {
                Message msg = (Message) userInput.readObject();

                switch (msg.getType()) {
                    case LOGIN:
                        this.login(msg);
                        break;
                    case REGISTER:
                        this.register(msg);
                        break;
                    case GET_ACTIVE:
                        this.sendActivePlayers();
                    default:
                        this.sendError("Unexpected error");
                        break;
                }
            }
        }catch (SocketException e) {
            e.printStackTrace(); // client shuts down
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendActivePlayers() throws IOException {
        Set<String> activePlayers = ServerAppMain.getOnlinePlayers();
        HashSet<String> players = new HashSet<>(activePlayers);
        players.remove(user.getUsername());
        this.userOutput.writeObject(players);
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
        ServerAppMain.addToActivePlayers(user);
        //inGameScene(userAndPass[0], userAndPass[1]);
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
        ServerAppMain.addToActivePlayers(user);
        //inGameScene(userAndPass[0], userAndPass[1]);
    }
    private void inGameScene(String username){
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

}