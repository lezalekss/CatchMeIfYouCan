package com.rmt.services;

import com.rmt.domain.Message;
import javafx.collections.ObservableSet;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

public class CommunicationService {

    private static CommunicationService serviceInstance = null;
    private Socket communicationSocket = null;
    private ObjectInputStream serverInput = null;
    private ObjectOutputStream serverOutput = null;

    private CommunicationService(){}

    public static CommunicationService getCommunicationServiceInstance() {
        if (serviceInstance == null) {
            serviceInstance = new CommunicationService();
        }
        return serviceInstance;
    }

    public boolean connect(){
        try {
            this.communicationSocket = new Socket("localhost", 5000);
            this.serverInput = new ObjectInputStream(this.communicationSocket.getInputStream());
            this.serverOutput = new ObjectOutputStream(this.communicationSocket.getOutputStream());
        }catch(UnknownHostException e){
            return false;
        }catch(IOException e1){
            return false;
        }
        return true;
    }

     public boolean register(String username, String password) {
//      #TODO encript password before sending
         try {
             this.sendMessage(Message.MessageType.REGISTER, username + "#" + password);
             Message answer = (Message) this.serverInput.readObject();
             switch (answer.getType()) {
                 case ANSWERS: {
                     return true;
                 }
                 case ERROR: {
                     return false;
                 }
             }
         } catch (ClassNotFoundException e) {
             return false;
         } catch (IOException e1){
             return false;
         }
         return false;
     }

    public String login(String username, String password) {
//      #TODO check whether username contains : or # before sending
        try {
            this.sendMessage(Message.MessageType.LOGIN,username+"#"+password);
            Message answer = (Message) this.serverInput.readObject();
            return answer.getMessageText();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(Message.MessageType messageType, String messageText) throws IOException {
        Message message = new Message(messageType, messageText);
        this.serverOutput.writeObject(message);
    }

    public Set<String> getActivePlayers() {
        try {
            this.sendMessage(Message.MessageType.GET_ACTIVE, "");
            Set<String> activePlayers = (Set<String>) this.serverInput.readObject();
            return activePlayers;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
