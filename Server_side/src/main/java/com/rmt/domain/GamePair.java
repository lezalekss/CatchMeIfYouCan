package com.rmt.domain;

import com.rmt.QuestionService;

public class GamePair {
    private QuestionService questionService = QuestionService.getQuestionServiceInstance();

    // Klasa koja ce cuvati sve informacije o paru koji je u igri
    private String firstPlayer;
    private String secondPlayer;
    private int firstPlayerCorrectAnswers;
    private int secondPlayerCorrectAnswers;
    private Question[] quickQuestions;
    private Question[] chaseQuestions;
    
    public GamePair(String pairUsernames) {
        String users[] = pairUsernames.split("#");
        this.firstPlayer = users[0];
        this.secondPlayer = users[1];
        this.firstPlayerCorrectAnswers = -1;
        this.secondPlayerCorrectAnswers = -1;
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

    public synchronized Question [] getQuickQuestions(){
        if(this.quickQuestions == null)
            this.quickQuestions = this.questionService.getRandomQuestions(QuestionService.Question_Type.QUICK);
        return this.quickQuestions;
    }

    public synchronized Question [] getChaseQuestions(){
        if(this.chaseQuestions == null)
            this.chaseQuestions = this.questionService.getRandomQuestions(QuestionService.Question_Type.CHASE);
        return this.chaseQuestions;
    }


    private boolean setFirstPlayerAnswers(int correctAnswers){
        this.firstPlayerCorrectAnswers=correctAnswers;
        if(secondPlayerCorrectAnswers>=0)
            return true;
        else return false;
    }
    private boolean setSecondPlayerAnswers(int correctAnswers){
        this.secondPlayerCorrectAnswers=correctAnswers;
        if(firstPlayerCorrectAnswers>=0)
            return true;
        else return false;
    }
}
