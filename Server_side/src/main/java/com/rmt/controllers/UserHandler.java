package com.rmt.controllers;
import com.rmt.ServerAppMain;
import com.rmt.domain.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;


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

            String request = this.hostInput.readLine();

            if (request.contains("100")) {
                this.hostOutput.println("101:Connection successful");
            }
        }catch (IOException e) {
//            e.printStackTrace();
        }
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
