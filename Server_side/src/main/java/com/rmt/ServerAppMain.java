package com.rmt;

import com.rmt.controllers.UserHandler;
import com.rmt.domain.Player;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ServerAppMain {
    private static ObservableMap<String, Player> playersMap;
    private static boolean mapChanged = false;
    private static final int port = 5000;
    private static Logger logger = Logger.getLogger(ServerAppMain.class.getName());

    public static void main(String[] args) {
        playersMap = FXCollections.observableMap(new HashMap());

        //addPlayersChangedListener();

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

    private static void addPlayersChangedListener() {
        playersMap.addListener((MapChangeListener<? super String, ? super Player>) change -> {
                    mapChanged = true;
                    logger.info("Map changed");
                }
        );
    }

    public static synchronized HashSet<String> getOnlinePlayers() {
        //return playersMap.values().stream().collect(Collectors.toList());
        HashSet<String> activePlayersUsernames = new HashSet<>(playersMap.keySet());
        return activePlayersUsernames;
    }

    public static synchronized void addToActivePlayers(Player player) {
        playersMap.put(player.getUsername(), player);
        if (playersMap.size() == 1) {
            addPlayersChangedListener();
        }
    }

    public static synchronized Socket findSocket(String username) {
        return playersMap.get(username).getSocket();
    }

    public static synchronized boolean isMapChanged() {
        return mapChanged;
    }

    public static synchronized void removePlayerFromActive(String username) {
        playersMap.remove(username);
    }

    public static synchronized void setMapChanged(boolean mapChanged) {
            mapChanged = mapChanged;
    }
}