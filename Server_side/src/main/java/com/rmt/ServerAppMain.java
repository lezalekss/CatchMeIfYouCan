package com.rmt;

import com.rmt.controllers.UserHandler;
import com.rmt.domain.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ServerAppMain {
    private static Map<String,Player> playersMap;
    private static final int port = 5000;
    private static Logger logger = Logger.getLogger(ServerAppMain.class.getName());

    public static void main(String[] args) {
        playersMap = new HashMap<>();



        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server is waiting for client");

            while (true) {
                Socket communicationSocket = serverSocket.accept();
                UserHandler user = new UserHandler(communicationSocket);
                user.start();
                logger.info("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized  List<Player> getOnlinePlayers() {
        return playersMap.values().stream().collect(Collectors.toList());
    }
    public static synchronized void addToActivePlayers(Player player) {
        playersMap.put(player.getUsername(),player);
    }
    public static synchronized Socket findSocket(String username){
        return playersMap.get(username).getSocket();
    }
}