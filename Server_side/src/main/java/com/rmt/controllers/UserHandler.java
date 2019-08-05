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


    public UserHandler(Socket socket) {
        user = new Player(socket);
    }

    public void run() {
        try {
            DBConnection dbConn = new DBConnection();
            userOutput = new ObjectOutputStream(this.user.getSocket().getOutputStream());
            userInput = new ObjectInputStream(this.user.getSocket().getInputStream());
            while() {
                Message msg = (Message) userInput.readObject();
                switch (msg.getType()) {
                    case LOGIN:
                        login();
                        break;
                    case REGISTER:
                        register();
                        break;
                    default:
                        throwError("Unexpected error");
                        break;
                }
                //   addToActivePlayers();
                //   showActivePlayers();
                //  receiveDecision();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void login() throws IOException {

    }
    private void register() {

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