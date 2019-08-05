package com.rmt;

import com.rmt.domain.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private Player sender;
    private Player receiver;
    private MessageType type;
    private String messageText;

    public enum MessageType{
        LOGIN,REGISTER,ANSWERS,ERROR
    }
}