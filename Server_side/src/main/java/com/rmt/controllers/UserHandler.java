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
import java.net.SocketException;

public class UserHandler extends Thread {

    private Player user;

    private ObjectInputStream userInput;
    private ObjectOutputStream userOutput;

    private DBConnection dbConn;

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
                System.out.println("UH u velikom while-u\n");
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
                        //svakako je neaktivan ko izaziva
                        // ServerAppMain.removePlayerFromActive(this.user.getUsername());
                        this.challenge(msg.getMessageText());
                        break;
                    }
                    case CHALLENGE_ANSWER: {
                        System.out.println("UH izvaznog od izazvanog klijenta primio odg\n");

                        String[] messageText = msg.getMessageText().split("\n");

                        Player opponent = ServerAppMain.findOfflinePlayer(messageText[1]);
                        ObjectOutputStream challengerOutput = opponent.getUserOutput();
                        challengerOutput.writeObject(new Message(Message.MessageType.ANSWERS, messageText[0]));

                        System.out.println("UH posledio odg CS-u izazivaca\n");

                        if (messageText[0].equals("YES")) {
                            System.out.println("UH u startGame metodi\n");
                            // u start game izazivacev username mora da bude prvi, a ovaj koji je izazvan drugi,
                            // zbog pretrage u mapi u GameHandler klasi jer je to kljuc
                            this.startGame(String.format("%s#%s",opponent.getUsername(),user.getUsername()));
                            break;
                        } else {
                            ServerAppMain.removePlayerFromOffline(user.getUsername());
                            ServerAppMain.addToActivePlayers(user);
                            break;
                        }
                    }
                    case LOG_OUT: {

                    }
                    case SWITCH: {
                        if (msg.getMessageText().equals("TO WAITING")) {
                            this.user.setStatus(Player.PlayerStatus.ACTIVE);
                            ServerAppMain.removePlayerFromOffline(user.getUsername());
                            ServerAppMain.addToActivePlayers(user);
                            break;
                        } else if (msg.getMessageText().equals("TO CHALLENGING")) {
                            this.sendAnswer("STOP");
                            this.user.setStatus(Player.PlayerStatus.OFFLINE);
                            ServerAppMain.removePlayerFromActive(user.getUsername());
                            ServerAppMain.addToOfflinePlayers(user);
                            break;
                        }
                    }
                    //case GAME_ACCEPTED:{
                    //  startGame();
                    // }
                    default:
                        this.sendError("Unexpected error");
                        break;
                }
            }
        } catch (java.io.EOFException e) {
            //kad kliknem na x u klijentu iskoci ovo
            if(user.getStatus()== Player.PlayerStatus.ACTIVE){
                ServerAppMain.removePlayerFromActive(user.getUsername());
            }else{
                ServerAppMain.removePlayerFromOffline(user.getUsername());
            }
            System.out.println("Player " + user.getUsername() + " just exited");
        } catch (SocketException e) {
            //e.printStackTrace(); // client shuts down
            if(user.getStatus()== Player.PlayerStatus.ACTIVE){
                ServerAppMain.removePlayerFromActive(user.getUsername());
            }else{
                ServerAppMain.removePlayerFromOffline(user.getUsername());
            }
            System.out.println("Player " + user.getUsername() + " just exited");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame(String usernames) {
    }

    private void challenge(String opponentUsername) throws IOException, ClassNotFoundException {
//      check for opponent status first
        //ne bi trebalo da moze da se izazove neko ko je neaktivan
        if (ServerAppMain.isActive(opponentUsername) == false) {
            this.sendError("Opponent is not active anymore");
            return;
        }
        //ako ga izazivam menjam mu status da ga ne izazove neko drugi, ako odbije da mu vratim status

        Player opponent = ServerAppMain.findActivePlayer(opponentUsername);
        System.out.println("Naso protivnika u mapi\n");

        ServerAppMain.removePlayerFromActive(opponentUsername);
        ServerAppMain.addToOfflinePlayers(opponent);

        System.out.println("UH stavio izvanog u neaktivne");

        ObjectOutputStream opponentOutput = opponent.getUserOutput();

        System.out.println("UH: naso za izazvanog strimove");

        opponentOutput.writeObject(new Message(Message.MessageType.PLAY_WITH, user.getUsername()));
        opponentOutput.flush();

        System.out.println("UH poslo izvanom pw\n");
    }


    private void sendActivePlayers() throws IOException, ClassNotFoundException {
        HashSet<String> players = ServerAppMain.getOnlinePlayers();
        players.remove(user.getUsername());
        this.userOutput.writeObject(players);
        //TODO: Add some sleeping maybe
        // ServerAppMain.addToActivePlayers(user);
    }


    private void updateActivePlayers() throws IOException {
        //da ga neko ne bi izazvao dok se updatuje

//       #TODO POPRAVI OVO
        //ServerAppMain.removePlayerFromActive(this.user.getUsername());


        HashSet<String> players = ServerAppMain.getOnlinePlayers();
        players.remove(user.getUsername());

        this.userOutput.writeObject(players);
        this.userOutput.flush();

        System.out.println("Players sent\n");

//       #TODO POPRAVI OVO
        //ServerAppMain.addToActivePlayers(this.user);
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
        user.setStatus(Player.PlayerStatus.OFFLINE);
        ServerAppMain.addToOfflinePlayers(user);
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
        user.setStatus(Player.PlayerStatus.OFFLINE);
        ServerAppMain.addToOfflinePlayers(user);
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