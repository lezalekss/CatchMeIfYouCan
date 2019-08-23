package com.rmt;

import com.rmt.controllers.UserHandler;
import com.rmt.domain.GamePair;
import com.rmt.domain.Player;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;


public class ServerAppMain {
    private static ObservableMap<String, Player> activePlayersMap;
    private static ObservableMap<String, Player> offlinePlayersMap;
    private static boolean mapChanged = false;
    private static final int port = 5000;
    private static Logger logger = Logger.getLogger(ServerAppMain.class.getName());
    private static final ReentrantReadWriteLock mapLock = new ReentrantReadWriteLock(true);
    private static final ReentrantReadWriteLock mapChangedLock = new ReentrantReadWriteLock(true);

    public static void main(String[] args) {
        activePlayersMap = FXCollections.observableMap(new HashMap());
        offlinePlayersMap = FXCollections.observableMap(new HashMap());
        GamePair gp = new GamePair();
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
        mapLock.writeLock().lock();
        mapChangedLock.writeLock().lock();
        try {
            activePlayersMap.addListener((MapChangeListener<? super String, ? super Player>) change -> {
                        mapChanged = true;
                    }
            );
        } finally {
            mapLock.writeLock().unlock();
            mapChangedLock.writeLock().unlock();
        }
    }

    public static HashSet<String> getOnlinePlayers() {
        //return activePlayersMap.values().stream().collect(Collectors.toList());
        mapLock.readLock().lock();
        try {
            HashSet<String> activePlayersUsernames = new HashSet<>(activePlayersMap.keySet());
            return activePlayersUsernames;
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public static void addToActivePlayers(Player player) {
        mapLock.writeLock().lock();
        try {
            activePlayersMap.put(player.getUsername(), player);
            if (activePlayersMap.size() == 1)
                addPlayersChangedListener();
        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public static void addToOfflinePlayers(Player player) {
        mapLock.writeLock().lock();
        try {
            offlinePlayersMap.put(player.getUsername(), player);
        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public static Socket findSocket(String username) {
        mapLock.readLock().lock();
        try {
            return activePlayersMap.get(username).getSocket();
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public static Player findActivePlayer(String username) {
        mapLock.readLock().lock();
        try {
            return activePlayersMap.get(username);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public static Player findOfflinePlayer(String username) {
        mapLock.readLock().lock();
        try {
            return offlinePlayersMap.get(username);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public static boolean isActive(String username) {
        mapLock.readLock().lock();
        try {
            return activePlayersMap.get(username) != null;
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public static boolean isMapChanged() {
        mapChangedLock.readLock().lock();
        try {
            return mapChanged;
        } finally {
            mapChangedLock.readLock().unlock();
        }
    }

    public static void removePlayerFromActive(String username) {
        mapLock.writeLock().lock();
        try {
            activePlayersMap.remove(username);
        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public static void removePlayerFromOffline(String username) {
        mapLock.writeLock().lock();
        try {
            offlinePlayersMap.remove(username);
        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public static void setMapChanged(boolean mapChanged) {
        mapChangedLock.writeLock().lock();
        try {
            mapChanged = mapChanged;
        } finally {
            mapChangedLock.writeLock().unlock();
        }
    }
}