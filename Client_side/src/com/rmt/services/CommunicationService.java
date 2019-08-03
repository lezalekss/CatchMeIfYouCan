package com.rmt.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class CommunicationService {

    private static CommunicationService serviceInstance = null;
    private Socket communicationSocket = null;
    private BufferedReader serverInput = null;
    private PrintStream serverOutput = null;

    public static CommunicationService getCommunicationServiceInstance() {
        if (serviceInstance == null) {
            serviceInstance = new CommunicationService();
        }
        return serviceInstance;
    }

    public boolean connect() throws IOException {
        this.communicationSocket = new Socket("localhost", 30000);
        this.serverInput = new BufferedReader(new InputStreamReader(this.communicationSocket.getInputStream()));
        this.serverOutput = new PrintStream(this.communicationSocket.getOutputStream());
        return testConnection();
    }

    private boolean testConnection() throws IOException {
        this.serverOutput.println("100:Checking connection");
        String answer = this.serverInput.readLine();
        if (answer.contains("101")) {
            return true;
        }
        return false;
    }

}
