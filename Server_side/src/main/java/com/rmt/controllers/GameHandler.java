package com.rmt.controllers;

import com.rmt.domain.GamePair;

import java.util.HashMap;
import java.util.Map;

public class GameHandler {
    // U ova klasi ce se ucitavati pitanja iz JSON-a
    private static Map<String,GamePair> pairMap;
    private static String questions[];
    public GameHandler(){
        pairMap = new HashMap<>();
    }

    public static synchronized GamePair addPairToMap(String pairUsernames){
        // ovde ce se takodje GamePair-u prosledjivati random pitanja iz pitanja
       return pairMap.computeIfAbsent(pairUsernames, k-> new GamePair(pairUsernames,questions));
    }
}
