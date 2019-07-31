package com.rmt;

import com.rmt.controllers.UserHandler;
import com.rmt.domain.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerAppMain {
    private static List<Player> players = null;

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ceka na konekciju...");

            while (true) {
                Socket communicationSocket = serverSocket.accept();
                UserHandler user = new UserHandler(communicationSocket);
                user.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Player> getPlayers() {
        if (players == null){
            players= new LinkedList<>();
        }
        return players;
    }
}
