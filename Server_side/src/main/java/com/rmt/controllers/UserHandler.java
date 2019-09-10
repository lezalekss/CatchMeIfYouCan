package com.rmt.controllers;

import com.rmt.DBConnection;
import com.rmt.main.ServerAppMain;
import com.rmt.domain.GamePair;
import com.rmt.domain.Message;
import com.rmt.domain.Player;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

public class UserHandler extends Thread {

    private Player user;

    private ObjectInputStream userInput;
    private ObjectOutputStream userOutput;

    private GamePair gamePair;
    private DBConnection dbConn;
    private String opponentUsername;
    private ObjectOutputStream opponentOutputStream;

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
//                System.out.println("UH u velikom while-u\n");
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
                        //svakako je neaktivan ko izaziva
                        // ServerAppMain.removePlayerFromActive(this.user.getUsername());
                        this.challenge(msg.getMessageText());
                        break;
                    }
                    case CHALLENGE_ANSWER: {
                        String[] messageText = msg.getMessageText().split("\n");

                        Player challenger = ServerAppMain.findOfflinePlayer(messageText[1]);
                        ObjectOutputStream challengerOutput = challenger.getUserOutput();
                        challengerOutput.writeObject(new Message(Message.MessageType.ANSWERS, messageText[0]));

                        if (messageText[0].equals("YES")) {
                            // u start game izazivacev username mora da bude prvi, a ovaj koji je izazvan drugi,
                            // zbog pretrage u mapi u GameHandler klasi jer je to kljuc
                            this.opponentUsername = messageText[1];
                            this.opponentOutputStream = challengerOutput;
                            this.startGame(String.format("%s#%s", challenger.getUsername(), user.getUsername()), false);
                            break;
                        } else {
                            ServerAppMain.removePlayerFromOffline(user.getUsername());
                            ServerAppMain.addToActivePlayers(user);
                            break;
                        }
                    }
                    case LOG_OUT: {
                        System.out.println("Before removing");
                        System.out.println("\nActive players");
                        for (String s:ServerAppMain.getActivePlayers()) {
                            System.out.println(s);
                        }
                        System.out.println();
                        System.out.println("\nOffline players");
                        for (String s:ServerAppMain.getOfflinePlayers()) {
                            System.out.println(s);
                        }

                        if(this.user.getStatus() == Player.PlayerStatus.OFFLINE) {
                            System.out.println("Status: offline");
                            ServerAppMain.removePlayerFromOffline(user.getUsername());
                        } else if(this.user.getStatus() == Player.PlayerStatus.ACTIVE) {
                            System.out.println("Status: active");
                            this.sendAnswer("STOP");
                            ServerAppMain.removePlayerFromActive(user.getUsername());
                        }
                        System.out.println("\nActive players");
                        for (String s:ServerAppMain.getActivePlayers()) {
                            System.out.println(s);
                        }
                        System.out.println();
                        System.out.println("\nOffline players");
                        for (String s:ServerAppMain.getOfflinePlayers()) {
                            System.out.println(s);
                        }


                        this.user.setUsername("");
                        this.user.setStatus(Player.PlayerStatus.OFFLINE);
                        break;
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
                    case GAME_ACCEPTED: {
                        //starts method for game managing for challenger, message text is challenged username
                        this.opponentUsername = msg.getMessageText();
                        this.opponentOutputStream = ServerAppMain.findOfflinePlayer(opponentUsername).getUserOutput();
                        this.startGame(String.format("%s#%s", user.getUsername(), this.opponentUsername), true);
                        break;
                    }
                    default:
                        this.sendError("Unexpected error");
                        break;
                }
            }
        } catch (java.io.EOFException e) {
            //kad kliknem na x u klijentu iskoci ovo
            if (user.getStatus() == Player.PlayerStatus.ACTIVE) {
                ServerAppMain.removePlayerFromActive(user.getUsername());
            } else {
                ServerAppMain.removePlayerFromOffline(user.getUsername());
            }
            System.out.println("EOF: Player " + user.getUsername() + " just exited");
        } catch (SocketException e) {
            //e.printStackTrace(); // client shuts down
            if (user.getStatus() == Player.PlayerStatus.ACTIVE) {
                ServerAppMain.removePlayerFromActive(user.getUsername());
            } else {
                ServerAppMain.removePlayerFromOffline(user.getUsername());
            }
            System.out.println("SE: Player " + user.getUsername() + " just exited");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame(String usernames, boolean isThisChallenger) {
        // startovanje prve igre
        this.gamePair = GameHandler.addPairToMap(usernames);
        System.out.println("UH: Game started and pair created\n");
        try {
            while (true) {
                Message msg = (Message) this.userInput.readObject();
                switch (msg.getType()) {
                    case GET_QUICK_QUESTIONS: {
                        this.userOutput.writeObject(this.gamePair.getQuickQuestions());
                        break;
                    }
                    case SET_CORRECT_ANSWERS: {
                        int correctAnswers = Integer.parseInt(msg.getMessageText()), opponentAnswers;
//                        System.out.println(user.getUsername()+"'s UH received "+correctAnswers+" correct answers");

                        boolean opponentFinished = this.gamePair.setPlayerCorrectAnswers(this.user.getUsername(), correctAnswers);
//                        System.out.println(user.getUsername()+"'s Opponent "+opponentUsername+" finished: "+opponentFinished);

                        opponentAnswers = opponentFinished ? this.gamePair.getOpponentsCorrectAnswers(this.user.getUsername()) : waitFewSecondsMore();
//                        System.out.println(user.getUsername()+"'s UH: opponents answers "+opponentAnswers);

                        if(correctAnswers > opponentAnswers){
                            this.sendAnswer(this.user.getUsername()+"#"+this.opponentUsername
                                            +"\n"+true+"#"+"false");
                            break;
                        }else if(opponentAnswers > correctAnswers){
                            this.sendAnswer(this.opponentUsername+"#"+this.user.getUsername()
                                            +"\n"+false+"#"+true);
                            break;
                        }else{
                            //ako imaju jednako poena, challenger je chaser
                            this.sendAnswer(usernames+"\n"+isThisChallenger+"#"+!isThisChallenger);
                            break;
                        }
                    }
                    case GET_CHASE_QUESTIONS: {
                        this.userOutput.writeObject(this.gamePair.getChaseQuestions());
                        break;
                    }
                    case EXCHANGE_ANSWERS:{
                        this.opponentOutputStream.writeObject(msg);
                        break;
                    }
                    case GAME_FINISHED:
                        return;
                }
            }
        }catch (EOFException e){
            ServerAppMain.removePlayerFromOffline(user.getUsername());
            System.out.println("SG - EOF: Player " + user.getUsername() + " just exited");
        }catch (SocketException e) {
            //e.printStackTrace(); // client shuts down
            ServerAppMain.removePlayerFromOffline(user.getUsername());
            System.out.println("SG - SE: Player " + user.getUsername() + " just exited");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (TimeoutException te) {
            // NISU UPISANI ODGOVORI DRUGOG IGRACA (VEROVATNO JE IZASAO)
        }
    }
    private int waitFewSecondsMore() throws TimeoutException {
        try {
            this.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            throw new TimeoutException();
        }
        return this.gamePair.getOpponentsCorrectAnswers(this.user.getUsername());
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
//        System.out.println("Naso protivnika u mapi\n");

        ServerAppMain.removePlayerFromActive(opponentUsername);
        ServerAppMain.addToOfflinePlayers(opponent);

//        System.out.println("UH stavio izvanog u neaktivne");

        ObjectOutputStream opponentOutput = opponent.getUserOutput();

//        System.out.println("UH: naso za izazvanog strimove");

        opponentOutput.writeObject(new Message(Message.MessageType.PLAY_WITH, user.getUsername()));
        opponentOutput.flush();

//        System.out.println("UH poslo izvanom pw\n");
    }


    private void sendActivePlayers() throws IOException {
        HashSet<String> players = ServerAppMain.getActivePlayers();
        players.remove(user.getUsername());
        this.userOutput.writeObject(players);

        // ServerAppMain.addToActivePlayers(user);
    }


    private void updateActivePlayers() throws IOException {
        //da ga neko ne bi izazvao dok se updatuje

//       #TODO POPRAVI OVO
        //ServerAppMain.removePlayerFromActive(this.user.getUsername());


        HashSet<String> players = ServerAppMain.getActivePlayers();
        players.remove(user.getUsername());

        this.userOutput.writeObject(players);
        this.userOutput.flush();

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