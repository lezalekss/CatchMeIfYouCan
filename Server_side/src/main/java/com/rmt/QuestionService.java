package com.rmt;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.rmt.domain.Question;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class QuestionService {

    public enum Question_Type {
        CHASE, QUICK
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final int quickQuestionsNumber = 20;
    private final int chaseQuestionsNumber = 10;

    private static QuestionService questionService;

    private QuestionService() {
    }

    public static QuestionService getQuestionServiceInstance() {
        if (questionService == null) {
            questionService = new QuestionService();
        }
        return questionService;
    }

    public Question[] getRandomQuestions(Question_Type question_type){
        int howManyQuestions;
        Question[] allQuestions;
        if(question_type == Question_Type.CHASE) {
            howManyQuestions = this.chaseQuestionsNumber;
            allQuestions = this.getQuestions(Question_Type.CHASE);
        }
        else {
            howManyQuestions = this.quickQuestionsNumber;
            allQuestions = this.getQuestions(Question_Type.QUICK);
        }

        int[] randomIndexes = this.getRandomNumbers(howManyQuestions, allQuestions.length);

        Question[] randomQuestions = new Question[randomIndexes.length];

        for (int i = 0; i < randomIndexes.length; i++) {
            randomQuestions[i] = allQuestions[randomIndexes[i]];
        }
        return randomQuestions;
    }

    private Question[] getQuestions(Question_Type question_type) {
        JsonArray jsonQuestions = this.getJsonQuestions(question_type);
        Question[] questions;
        if (jsonQuestions == null) {
            questions = new Question[0];
        } else {
            questions = gson.fromJson(jsonQuestions, Question[].class);
        }
        return questions;
    }

    private JsonArray getJsonQuestions(Question_Type question_type) {
        JsonArray jsonQuestions = null;
        try (JsonReader reader = new JsonReader(new FileReader("/home/jeca/IdeaProjects/Git/CatchMeIfYouCan/Server_side/data/questions.json"))) {
            JsonElement fileContent = gson.fromJson(reader, JsonObject.class);
            if (question_type == Question_Type.QUICK)
                jsonQuestions = fileContent.getAsJsonObject().get("QuickQuestions").getAsJsonArray();
            else
                jsonQuestions = fileContent.getAsJsonObject().get("ChaseQuestions").getAsJsonArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonQuestions;
    }

    private int[] getRandomNumbers(int howMany, int range){
        int[] array = new int[howMany];
        Random random = new Random();
        int counter = 0;
        while(counter < howMany){
            int randomNumber = random.nextInt(range);
            if(isNumberAlreadyChosen(array, randomNumber) == false)
                array[counter++] = randomNumber;
        }
        return array;
    }

    private boolean isNumberAlreadyChosen(int[] array, int number) {
        for (int i = 0; i < array.length; i++) {
            if(array[i] == number)
                return true;
        }
        return false;
    }




}
