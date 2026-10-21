package com.trivia.masterandroid.data.remote;

import com.trivia.masterandroid.data.remote.dto.CategoryDto;
import com.trivia.masterandroid.data.remote.dto.QuestionDto;
import com.trivia.masterandroid.data.remote.dto.TriviaResponse;
import com.trivia.masterandroid.data.repo.TriviaException;
import com.trivia.masterandroid.model.Question;
import com.trivia.masterandroid.util.HtmlUtil;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/** Blocking calls: always invoke from a background thread. */
public class RemoteDataSource {

    private final OpenTdbApi api = RetrofitClient.getApi();

    public List<Question> fetchQuestions(int amount, int categoryId, String difficulty)
            throws TriviaException {
        Integer category = categoryId > 0 ? categoryId : null;
        String diff = (difficulty == null || difficulty.isEmpty()) ? null : difficulty;
        TriviaResponse body = execute(api.getQuestions(amount, category, diff, "multiple"));
        validate(body.responseCode);
        if (body.results == null || body.results.isEmpty()) {
            throw new TriviaException(TriviaException.Type.NO_RESULTS);
        }
        List<Question> questions = new ArrayList<>();
        for (QuestionDto dto : body.results) {
            if (dto.question == null || dto.correctAnswer == null || dto.incorrectAnswers == null) {
                continue;
            }
            List<String> wrong = new ArrayList<>();
            for (String w : dto.incorrectAnswers) {
                wrong.add(HtmlUtil.decode(w));
            }
            questions.add(new Question(HtmlUtil.decode(dto.category), dto.difficulty,
                    HtmlUtil.decode(dto.question), HtmlUtil.decode(dto.correctAnswer), wrong));
        }
        if (questions.isEmpty()) {
            throw new TriviaException(TriviaException.Type.PARSE);
        }
        return questions;
    }

    public List<CategoryDto> fetchCategories() throws TriviaException {
        CategoryDto.Response body = execute(api.getCategories());
        if (body.categories == null) {
            throw new TriviaException(TriviaException.Type.PARSE);
        }
        return body.categories;
    }

    private static void validate(int code) throws TriviaException {
        switch (code) {
            case 0:
                return;
            case 1:
                throw new TriviaException(TriviaException.Type.NO_RESULTS);
            case 2:
                throw new TriviaException(TriviaException.Type.INVALID_PARAMETER);
            case 5:
                throw new TriviaException(TriviaException.Type.RATE_LIMIT);
            default:
                throw new TriviaException(TriviaException.Type.SERVER);
        }
    }

    private static <T> T execute(retrofit2.Call<T> call) throws TriviaException {
        try {
            Response<T> response = call.execute();
            if (response.code() == 429) {
                throw new TriviaException(TriviaException.Type.RATE_LIMIT);
            }
            if (!response.isSuccessful() || response.body() == null) {
                throw new TriviaException(TriviaException.Type.SERVER);
            }
            return response.body();
        } catch (SocketTimeoutException | InterruptedIOException e) {
            throw new TriviaException(TriviaException.Type.TIMEOUT, e);
        } catch (IOException e) {
            throw new TriviaException(TriviaException.Type.NO_CONNECTION, e);
        } catch (RuntimeException e) {
            throw new TriviaException(TriviaException.Type.PARSE, e);
        }
    }
}
