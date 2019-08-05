package com.rmt;

import com.rmt.domain.Player;

import java.io.Serializable;

public class Message implements Serializable {
    private Player sender;
    private Player receiver;
    private MessageType type;
    private String messageText;
    public Message(){

    }
    public Message(Player sender, Player receiver, MessageType type, String messageText) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.messageText = messageText;
    }

    public Player getSender() {
        return sender;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public void setReceiver(Player receiver) {
        this.receiver = receiver;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }



    public enum MessageType{
        LOGIN,REGISTER,ANSWERS,ERROR
    }
}