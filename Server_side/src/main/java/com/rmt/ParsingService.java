package com.rmt;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rmt.domain.Question;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ParsingService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static ParsingService parsingService;

    private ParsingService() {
    }

    public ParsingService getParsingServiceInstance() {
        if (parsingService == null) {
            parsingService = new ParsingService();
        }
        return parsingService;
    }

    private JsonArray getJsonQuestions() {
        JsonArray jsonQuestions = null;
        try (JsonReader reader = new JsonReader(new FileReader("/home/jeca/IdeaProjects/Git/CatchMeIfYouCan/Server_side/data/questions.json"))) {
            JsonObject fileContent = gson.fromJson(reader, JsonObject.class);
            jsonQuestions = fileContent.get("Question").getAsJsonArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonQuestions;
    }

    public Question[] getQuestions() {
        JsonArray jsonQuestions = this.getJsonQuestions();
        Question[] questions;
        if (jsonQuestions == null) {
            questions = new Question[0];
        } else {
            questions = gson.fromJson(jsonQuestions, Question[].class);
        }
        return questions;
    }
}
