package com.trivia.masterandroid.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionDto {

    public String category;
    public String type;
    public String difficulty;
    public String question;

    @SerializedName("correct_answer")
    public String correctAnswer;

    @SerializedName("incorrect_answers")
    public List<String> incorrectAnswers;
}
