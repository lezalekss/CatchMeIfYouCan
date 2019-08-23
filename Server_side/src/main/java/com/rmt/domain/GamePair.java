package com.rmt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamePair {
    // Klasa koja ce cuvati sve informacije o paru koji je u igri
    private String firstPlayer;
    private String secondPlayer;
    private String questions[]; //simulacija pitanja kasnije ce ovo biti iz json-a popunjeno

    public GamePair(String pairUsernames, String questions[]) {
        String users[] = pairUsernames.split("#");
        this.firstPlayer = users[0];
        this.secondPlayer = users[1];
        this.questions = questions;
    }

}
