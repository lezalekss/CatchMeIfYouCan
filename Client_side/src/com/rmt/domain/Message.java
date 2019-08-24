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
        LOGIN,REGISTER,LOG_OUT,ANSWERS,ERROR,GET_ACTIVE,UPDATE_ACTIVE,PLAY_WITH,CHALLENGE_ANSWER,
        SWITCH,GAME_ACCEPTED
    }
}