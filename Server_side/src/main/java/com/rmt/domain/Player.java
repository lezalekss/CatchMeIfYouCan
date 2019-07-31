package com.rmt.domain;

import java.net.Socket;

public class Player {

    public enum PlayerStatus{ACTIVE, PLAYING}


    private Socket socket;
    private String username;
    private PlayerStatus status;

    public Player(Socket socket, String username, PlayerStatus status) {
        this.socket = socket;
        this.username = username;
        this.status = status;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
}

