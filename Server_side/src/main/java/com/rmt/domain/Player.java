package com.rmt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public enum PlayerStatus{ACTIVE, PLAYING,OFFLINE}

    private Socket socket;
    private String username;
    private PlayerStatus status;

    public Player(Socket socket){
        this.socket=socket;
    }
}
