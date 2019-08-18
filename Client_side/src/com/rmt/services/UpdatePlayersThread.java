package com.rmt.services;

import com.rmt.domain.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;


public class UpdatePlayersThread extends Thread {

    private final ObjectOutputStream serverOutput;
    private final ObjectInputStream serverInput;
    private ObservableSet<String> players;

    public UpdatePlayersThread(ObjectOutputStream serverOutput, ObjectInputStream serverInput, ObservableSet<String> players) {
        this.serverOutput = serverOutput;
        this.serverInput = serverInput;
        this.players = players;
    }

    @Override
    public void run() {
        System.out.println("Updating active\n");
        while (this.isInterrupted() == false) {
            try {
                this.sleep(10000);
                this.serverOutput.writeObject(new Message(Message.MessageType.GET_ACTIVE, ""));
                System.out.println("Request sent\n");
                Message answer = (Message) this.serverInput.readObject();
                System.out.println("Answer received\n");
                if (answer.getMessageText().equals("OK")) {
                    System.out.println("Map changed\n");
                    HashSet<String> updatedPlayers = (HashSet<String>) this.serverInput.readObject();
                    this.players.addAll(updatedPlayers);
                    System.out.println("Set updated from thread\n");
                } else
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Thread is shutting down\n");
                return;
            }
        }
        System.out.println("Thread is shutting down\n");
    }
}
