package com.trivia.masterandroid.util;

import com.trivia.masterandroid.R;
import com.trivia.masterandroid.data.repo.TriviaException;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static int of(TriviaException e) {
        switch (e.type) {
            case NO_CONNECTION:
            case NO_OFFLINE_DATA:
                return R.string.error_no_connection;
            case TIMEOUT:
                return R.string.error_timeout;
            case NO_RESULTS:
                return R.string.error_no_results;
            case INVALID_PARAMETER:
                return R.string.error_invalid_parameter;
            case RATE_LIMIT:
                return R.string.error_rate_limit;
            case PARSE:
                return R.string.error_parse;
            default:
                return R.string.error_server;
        }
    }
}
