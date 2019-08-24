package com.rmt.domain;

public class GamePair {
    // Klasa koja ce cuvati sve informacije o paru koji je u igri
    private String firstPlayer;
    private String secondPlayer;
    private String qucikQuestions[]; //simulacija pitanja kasnije ce ovo biti iz json-a popunjeno
    private int firstPlayerCorrectAnswers;
    private int secondPlayerCorrectAnswers;
    
    public GamePair(String pairUsernames, String qucikQuestions[]) {
        String users[] = pairUsernames.split("#");
        this.firstPlayer = users[0];
        this.secondPlayer = users[1];
        this.qucikQuestions = qucikQuestions;
    }

    public synchronized boolean setPlayerCorrectAnswers(String username, int correctAnswers){
        // metoda setuje tacne odgovore igraca i vraca true ako su setovana oba polja
        // i false ako nisu(znaci da treba da saceka jos 5 sekundi pa da pita opet)
        return username.equals(firstPlayer)?setFirstPlayerAnswers(correctAnswers):setSecondPlayerAnswers(correctAnswers);
    }

    public synchronized int getPlayerCorrectAnswers(String username){
        return username.equals(firstPlayer)?firstPlayerCorrectAnswers:secondPlayerCorrectAnswers;
    }

    public synchronized int getOpponentsCorrectAnswers(String username){
        return username.equals(firstPlayer)?secondPlayerCorrectAnswers:firstPlayerCorrectAnswers;
    }

    public synchronized String [] getQucikQuestions(){
        return this.qucikQuestions;
    }

    private boolean setFirstPlayerAnswers(int correctAnswers){
        this.firstPlayerCorrectAnswers=correctAnswers;
        if(secondPlayerCorrectAnswers>0)
            return true;
        else return false;
    }
    private boolean setSecondPlayerAnswers(int correctAnswers){
        this.secondPlayerCorrectAnswers=correctAnswers;
        if(firstPlayerCorrectAnswers>0)
            return true;
        else return false;
    }
}
