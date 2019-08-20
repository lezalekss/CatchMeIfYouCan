package com.rmt.services;

import com.rmt.domain.Message;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ChallengeWaiter extends Thread {

    private final ObjectInputStream serverInput;
    private final ObjectOutputStream serverOutput;
    private BooleanProperty challengeSent;
    private BooleanProperty challengeReceived;
    //kao out parametar
    private String challengerUsername;
    private static final ReentrantReadWriteLock serverInputLock = new ReentrantReadWriteLock(true);


    public ChallengeWaiter(ObjectInputStream serverInput, ObjectOutputStream serverOutput, BooleanProperty challengeSent, BooleanProperty challengeReceived, String challengerUsername) {

        this.serverInput = serverInput;
        this.serverOutput = serverOutput;
        this.challengeSent = challengeSent;
        this.challengeReceived = challengeReceived;
        this.challengerUsername = challengerUsername;
    }

    @Override

    public void run() {
        System.out.println("CW: aktivan \n");

        //this.addChallengeSentListener();
        try {
            while (this.isInterrupted() == false) {
//                if (this.serverInput.available() != 0) {

//                    serverInputLock.readLock().lock();
//                    try {
                        Message msg = (Message) this.serverInput.readObject();

                        System.out.println("CW: poziv procitan\n");

                        if (msg.getType() == Message.MessageType.PLAY_WITH) {

                            this.challengerUsername = msg.getMessageText();

                            System.out.println("CW: postavio challenger username");

                            this.challengeReceived.setValue(true);

                            System.out.println("CW postavio true za challenge received i gasi se");

                            return;
                        } else if(msg.getType() == Message.MessageType.ANSWERS && msg.getMessageText().equals("STOP")){
                            this.serverOutput.writeObject(new Message(Message.MessageType.ANSWERS, "STOPPED"));
                            return;
                        }
//                    } finally {
//                        serverInputLock.readLock().unlock();
//                    }
//                }
//                else {
//                    this.sleep(5000);
//                }
            }
//        } catch (InterruptedException e) {
//            System.out.println("CW se gasi\n");
//
//            return;
        } catch (IOException e) {
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addChallengeSentListener() {
        this.challengeSent.addListener((observable, oldValue, newValue) -> {
            if (newValue == true){
                System.out.println("CW uvatio true za challenge sent\n");
                this.challengeSent.setValue(false);
            }
        });
    }

}

