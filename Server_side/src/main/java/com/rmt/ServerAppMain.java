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
    private static final ReentrantReadWriteLock mapLock  = new ReentrantReadWriteLock(true);
    private static final ReentrantReadWriteLock mapChangedLock  = new ReentrantReadWriteLock(true);

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
		mapLock.writeLock().lock();
		mapChanged.writeLock().lock();
        try{
            HashSet<String> activePlayersUsernames = new HashSet<>(playersMap.keySet());
			return activePlayersUsernames;
        }finally {
            mapLock.writeLock().unlock();
			mapChanged.writeLock.unlock();
        }
        playersMap.addListener((MapChangeListener<? super String, ? super Player>) change -> {
                    mapChanged = true;
                    logger.info("Map changed");
                }
        );
    }

    public static HashSet<String> getOnlinePlayers() {
        //return playersMap.values().stream().collect(Collectors.toList());
		mapLock.readLock().lock();
        try{
            HashSet<String> activePlayersUsernames = new HashSet<>(playersMap.keySet());
        return activePlayersUsernames;
        }finally {
            mapLock.readLock().unlock();
        }
        
    }

    public static void addToActivePlayers(Player player) {
		mapLock.writeLock.lock()
		try{
			playersMap.put(player.getUsername(), player);
			if (playersMap.size() == 1)
				addPlayersChangedListener();
        }finally {
            mapLock.writeLock().unlock();
        }
    }

    public static Socket findSocket(String username) {
		mapLock.readLock().lock();
        try{
			return playersMap.get(username).getSocket();
        }finally {
            mapLock.readLock().unlock();
        }
    }

    public static boolean isMapChanged() {
		mapChangedLock.readLock().lock();
        try{
			return mapChanged;
        }finally {
            mapChangedLock.readLock().unlock();
        }
    }

    public static void removePlayerFromActive(String username) {
		mapLock.writeLock.lock()
		try{
			playersMap.remove(username);
        }finally {
            mapLock.writeLock().unlock();
        }
    }

    public static void setMapChanged(boolean mapChanged) {
        mapChangedLock.writeLock().lock();
        try{
			this.mapChanged = mapChanged;
        }finally {
            mapChangedLock.writeLock().unlock();
        }
    }
}