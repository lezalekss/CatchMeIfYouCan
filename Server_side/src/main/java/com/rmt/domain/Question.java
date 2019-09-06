package com.rmt.domain;

import lombok.Data;

@Data
public class Question {
  private String questionText;
  private String[] possibleAnswers;
  private String correctAnswer;
}
