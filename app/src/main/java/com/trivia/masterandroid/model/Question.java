package com.trivia.masterandroid.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question implements Serializable {

    public final String category;
    public final String difficulty;
    public final String text;
    public final String correctAnswer;
    public final List<String> options;

    public Question(String category, String difficulty, String text,
                    String correctAnswer, List<String> incorrectAnswers) {
        this.category = category;
        this.difficulty = difficulty;
        this.text = text;
        this.correctAnswer = correctAnswer;
        List<String> all = new ArrayList<>(incorrectAnswers);
        all.add(correctAnswer);
        Collections.shuffle(all);
        this.options = all;
    }

    public boolean isCorrect(String answer) {
        return correctAnswer.equals(answer);
    }
}
