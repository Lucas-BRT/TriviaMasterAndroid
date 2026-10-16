package com.trivia.masterandroid.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TriviaResponse {

    @SerializedName("response_code")
    public int responseCode;

    public List<QuestionDto> results;
}
