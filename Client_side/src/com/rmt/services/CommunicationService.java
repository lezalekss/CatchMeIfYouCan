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

    private CommunicationService(){}

    public static CommunicationService getCommunicationServiceInstance() {
        if (serviceInstance == null) {
            serviceInstance = new CommunicationService();
        }
        return serviceInstance;
    }

    public boolean connect() throws IOException {
        this.communicationSocket = new Socket("localhost", 5000);
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

    public boolean register(String username, String password) throws IOException {
//      #TODO encript password before sending
//      #TODO check whether username contains : or # before sending
        this.serverOutput.println("200:Username#"+username+":Password#"+password);
        String answer = this.serverInput.readLine();
        if(answer.contains("201")) {
            return true;
        }else{
//          #TODO find a way to pass the message extracted from answer to scene
            return false;
        }
    }

    public boolean login(String username, String password) throws IOException {
//      #TODO check whether username contains : or # before sending
        this.serverOutput.println("300:Username#"+username+":Password#"+password);
        String answer = this.serverInput.readLine();
        if(answer.contains("301")) {
            return true;
        }else{
//          #TODO find a way to pass the message extracted from answer to scene
            return false;
        }
    }
}
