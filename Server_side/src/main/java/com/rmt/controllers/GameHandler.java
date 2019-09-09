package com.rmt.controllers;

import com.rmt.QuestionService;
import com.rmt.domain.GamePair;

import com.rmt.domain.Question;
import java.util.HashMap;
import java.util.Map;

public class GameHandler {
    // U ova klasi ce se ucitavati pitanja iz JSON-a

    private static Map<String,GamePair> pairMap;
    public GameHandler(){
        pairMap = new HashMap<>();
    }

    public static synchronized GamePair addPairToMap(String pairUsernames){
       return pairMap.computeIfAbsent(pairUsernames, k-> new GamePair(pairUsernames));
    }
}
