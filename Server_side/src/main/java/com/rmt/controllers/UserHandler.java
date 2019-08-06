package com.rmt.controllers;
import com.rmt.DBConnection;
import com.rmt.Message;
import com.rmt.ServerAppMain;
import com.rmt.domain.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;


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
            while(true) {
                Message msg = (Message) userInput.readObject();
                switch (msg.getType()) {
                    case LOGIN:
                        login(msg);
                        break;
                    case REGISTER:
                        register(msg);
                        break;
                    default:
                        throwError("Unexpected error");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void login(Message msg)  {
        Message answer = new Message();
        answer.setType(Message.MessageType.LOGIN);
        String []userAndPass = msg.getMessageText().split("#");
        if(!dbConn.isRegistered(userAndPass[0],userAndPass[1]))
            inGameScene(userAndPass[0], userAndPass[1]);
        else throwError(String.format("User with name:%s and pass:%s does not exist",userAndPass[0],userAndPass[1]));
    }
    private void register(Message msg) {
        Message answer = new Message();
        answer.setType(Message.MessageType.REGISTER);
        String []userAndPass = msg.getMessageText().split("#");
        if(!dbConn.isRegistered(userAndPass[0])){
            dbConn.insertIntoDatabase(userAndPass[0],userAndPass[1]);
            inGameScene(userAndPass[0], userAndPass[1]);
        }
       else throwError(String.format("User with name:%s is already registered",userAndPass[0]));
    }
    private void inGameScene(String username, String pass){

    }

    private void throwError(String errorMsg){
        Message errorMessage = new Message();
        errorMessage.setMessageText(errorMsg);
        try {
            this.userOutput.writeObject(errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket findSocket(String username) {
       return ServerAppMain.findSocket(username);
    }

    private void showActivePlayers() {
        // this.hostOutput.println("Currently active players:");
        List<Player> players = ServerAppMain.getOnlinePlayers();
        for (Player player : players) {
            if (player.getUsername() != this.user.getUsername()) {
                //       this.hostOutput.println(player.getUsername());
            }
        }
    }

}