package com.rmt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private MessageType type;
    private String messageText;

    public enum MessageType{
        LOGIN,REGISTER,ANSWERS,ERROR,GET_ACTIVE,PLAYERS_CHANGED,PLAY_WITH,STOP_UPDATE
    }
}