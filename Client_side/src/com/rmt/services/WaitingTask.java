package com.rmt.services;

import com.rmt.domain.Message;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;

public class WaitingTask extends Task<String> {

    private ObjectInputStream serverInput;

    public WaitingTask(ObjectInputStream serverInput) {
        this.serverInput = serverInput;
    }

    public WaitingTask() {
    }

    public void setServerInput(ObjectInputStream serverInput) {
        this.serverInput = serverInput;
    }

    @Override
    protected String call() throws Exception {
        try {
            System.out.println("\nCW aktivan, ceka izazov");
            Object o = (Object)serverInput.readObject();
            if(o instanceof Message) {
                Message msg = (Message)o;

                if (msg.getType() == Message.MessageType.PLAY_WITH) {

                    return msg.getMessageText();

                } else if (msg.getType() == Message.MessageType.ANSWERS && msg.getMessageText().equals("STOP")) {
                    System.out.println("\nCW se gasi");
                    return "shutdown";
                }
            }else {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
