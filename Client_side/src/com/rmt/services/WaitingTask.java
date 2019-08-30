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
        System.out.println("CW: aktivan \n");

        try {
            Object o = (Object)serverInput.readObject();
            if(o instanceof Message) {
                Message msg = (Message)o;

                System.out.println("CW: poruka procitana\n");

                if (msg.getType() == Message.MessageType.PLAY_WITH) {

                    System.out.println("CW vraca username i gasi se");

                    return msg.getMessageText();

                } else if (msg.getType() == Message.MessageType.ANSWERS && msg.getMessageText().equals("STOP")) {

                    System.out.println("CW primio stop i gasi se\n");
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
