package com.trivia.masterandroid.model;

import java.io.Serializable;

public class AnswerRecord implements Serializable {

    public final String question;
    public final String chosen;
    public final String correctAnswer;
    public final boolean correct;
    public final boolean timedOut;
    public final int points;

    public AnswerRecord(String question, String chosen, String correctAnswer,
                        boolean correct, boolean timedOut, int points) {
        this.question = question;
        this.chosen = chosen;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.timedOut = timedOut;
        this.points = points;
    }
}
