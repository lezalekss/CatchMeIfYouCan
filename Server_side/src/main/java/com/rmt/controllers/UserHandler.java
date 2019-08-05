package com.rmt.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class UserHandler extends Thread {

    Socket hostSocket = null;
    String hostUsername = "";
    BufferedReader hostInput = null;
    PrintStream hostOutput = null;

    Socket guestSocket = null;
    String guestUsername = "";
    BufferedReader guestInput = null;
    PrintStream guestOutput = null;


    public UserHandler(Socket hostSocket) {
        this.hostSocket = hostSocket;
    }

    public UserHandler(Socket hostSocket, String hostUsername) {
        this.hostSocket = hostSocket;
        this.hostUsername = hostUsername;
    }

    public void run() {
        try {
            this.hostInput = new BufferedReader(new InputStreamReader(this.hostSocket.getInputStream()));
            this.hostOutput = new PrintStream(this.hostSocket.getOutputStream());

            while (true) {
                String request = this.hostInput.readLine();
//              #TODO find a more efficient way of checking what code request contains
                //potential problem in case when exp. password contains 100, not the "header"
                //we need some marker for header, # maybe
                if (request.contains("100")) {
                    this.hostOutput.println("101:Connection successful");
                    continue;
                } else if (request.contains("200")) {
//                  #TODO think about specifing method for extracting body of request from request
                    this.registration(request);
                    continue;
                } else if (request.contains("300")) {
//                  #TODO think about specifing method for extracting body of request from request
                    this.login(request);
                    continue;
                }
            }

        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private void login(String request) {
        String[] requestParts = request.split(":");
        String username = requestParts[1].split("#")[1];
//       #TODO decript password
        String password = requestParts[2].split("#")[1];
        if (this.usernameAlreadyExists(username) == false) {
            this.hostOutput.println("302:Login failed. Username doesn't exists.");
            return;
        }
        if (this.validatePassword(username, password) == false){
            this.hostOutput.println("303:Login failed. Incorrect password.");
            return;
        }
        this.hostOutput.println("301:Login successful.");
        this.hostUsername = username;
    }

    private boolean validatePassword(String username, String password) {
//        #TODO check whether the password matches the saved password for given username in the file with users credentials
        return true;
    }

    private void registration(String request) {
        String[] requestParts = request.split(":");
        String username = requestParts[1].split("#")[1];
//       #TODO decript password
        String password = requestParts[2].split("#")[1];
        if (this.usernameAlreadyExists(username) == true) {
            this.hostOutput.println("202:Registration failed. Username already exists.");
            return;
        }
        if (this.validatePasswordLength(password) == false) {
            this.hostOutput.println("203:Registration failed. Password has to contain minimum 8 characters.");
            return;
        }
        if (this.validatePasswordUpperAndLowerCases(password) == false) {
            this.hostOutput.println("204:Registration failed. Password has to contain upper and low cases.");
            return;
        }
        if (this.validatePasswordDigits(password) == false) {
            this.hostOutput.println("205:Registration failed. Password has to contain minimum 1 digit.");
            return;
        }
        this.saveUser(username, password);
        this.hostOutput.println("201:Registration successful.");
        this.hostUsername = username;
    }

    private void saveUser(String username, String password) {
    }

    private boolean validatePasswordDigits(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePasswordUpperAndLowerCases(String password) {
        boolean containsUpperCase = false;
        boolean containsLowerCase = false;

        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.codePointAt(i))) {
                containsUpperCase = true;
                if (containsLowerCase) {
                    break;
                } else {
                    containsLowerCase = this.containsLowerCase(password.substring(i + 1));
                    break;
                }
            } else if (Character.isLowerCase(password.codePointAt(i))) {
                containsLowerCase = true;
                if (containsUpperCase) {
                    break;
                } else {
                    containsUpperCase = this.containsUpperCase(password.substring(i + 1));
                    break;
                }
            }
        }
        return containsUpperCase && containsLowerCase;
    }

    private boolean containsLowerCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isUpperCase(string.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePasswordLength(String password) {
        return password.length() >= 8;
    }

    private boolean usernameAlreadyExists(String username) {
//        #TODO check whether there is a user with the given username in the file with users credentials
        return false;
    }

//            if (this.hostUsername.isEmpty()) {
//                login();
//            }
//            addHostToActivePlayers();
//            showActivePlayers();
//            receiveDecision();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void receiveDecision() throws IOException, InterruptedException {
//        String decision = this.hostInput.readLine();
//        if (decision.startsWith("#playWith")) {
//            String guestUsername = decision.split(":")[1];
//            Socket guestSocket = this.findSocket(guestUsername);
//            if (guestSocket == null) {
//                this.hostOutput.println("Igrac vise nije dostupan, izaberite drugog igraca");
//                return;
//            }
//            (new PrintStream(guestSocket.getOutputStream())).println(this.hostUsername + " vas izaziva. Da li prihvatate izazov?");
//            String answer = (new BufferedReader(new InputStreamReader
//                    (guestSocket.getInputStream()))).readLine();
//            if (answer.contains("ChallengeAccepted")) {
//                this.hostOutput.println(guestUsername + " je prihvatio izazov! Igra moze da pocne!");
//                (new PrintStream(guestSocket.getOutputStream())).println("Uspesno ste prihvatili izazov! Igra pocinje uskoro!");
//            } else if (answer.contains("ChallengeRejected")) {
//                this.hostOutput.println(guestUsername + " je odbio izazov!");
//                (new PrintStream(guestSocket.getOutputStream())).println("Uspesno ste odbili izazov!");
//            }
//        } else if (decision.contains("turnOff")) {
//            this.join();
//        } else if (decision.contains("continue")) {
//            return;
//        }
//    }
//
//    private Socket findSocket(String guestusername) {
//        List<Player> players = ServerAppMain.getPlayers();
//        for (Player player : players) {
//            if (player.getUsername().equals(guestusername)) {
//                return player.getSocket();
//            }
//        }
//        return null;
//    }
//
//    private void addHostToActivePlayers() {
//        Player host = new Player(this.hostSocket, this.hostUsername, Player.PlayerStatus.ACTIVE);
//        ServerAppMain.getPlayers().add(host);
//    }
//
//    private void showActivePlayers() {
//        this.hostOutput.println("Currently active players:");
//        List<Player> players = ServerAppMain.getPlayers();
//        for (Player player : players) {
//            if (player.getUsername() != this.hostUsername) {
//                this.hostOutput.println(player.getUsername());
//            }
//        }
//    }
//
//    private void login() throws IOException {
//        boolean repeat = true;
//        String username = "";
//        while (repeat) {
//            this.hostOutput.println("Unesite korisnicko ime: ");
//            username = this.hostInput.readLine();
//            if (username == null || username.isEmpty()) {
//                this.hostOutput.println("Uneli ste nevalidno korisnicko ime!");
//            } else {
//                this.hostOutput.println("Uspesno ste se prijavili!");
//                repeat = false;
//            }
//        }
//        this.hostUsername = username;
//    }
}
