package com.trivia.masterandroid.data.remote;

import com.trivia.masterandroid.data.remote.dto.CategoryDto;
import com.trivia.masterandroid.data.remote.dto.TriviaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenTdbApi {

    @GET("api.php")
    Call<TriviaResponse> getQuestions(
            @Query("amount") int amount,
            @Query("category") Integer category,
            @Query("difficulty") String difficulty,
            @Query("type") String type);

    @GET("api_category.php")
    Call<CategoryDto.Response> getCategories();
}
