package com.trivia.masterandroid.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryDto {

    public int id;
    public String name;

    public static class Response {
        @SerializedName("trivia_categories")
        public List<CategoryDto> categories;
    }
}
