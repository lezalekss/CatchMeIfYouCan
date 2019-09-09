package com.rmt.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Question implements Serializable {
    private String questionText;
    private String[] possibleAnswers;
    private String correctAnswer;
}
