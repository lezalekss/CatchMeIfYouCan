package com.rmt.main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.rmt.QuestionService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.*;


public class Test {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        JsonArray jsonQuestions = null;
        try (JsonReader reader = new JsonReader(new FileReader("/home/jeca/IdeaProjects/Git/CatchMeIfYouCan/Server_side/data/questions.json"))) {
            JsonElement fileContent = gson.fromJson(reader, JsonObject.class);
            jsonQuestions = fileContent.getAsJsonObject().get("ChaseQuestions").getAsJsonArray();
//                jsonQuestions = fileContent.getAsJsonObject().get("ChaseQuestions").getAsJsonArray();
            System.out.println(jsonQuestions.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
