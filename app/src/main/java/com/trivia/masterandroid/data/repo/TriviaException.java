package com.trivia.masterandroid.data.repo;

public class TriviaException extends Exception {

    public enum Type { NO_CONNECTION, TIMEOUT, SERVER, NO_RESULTS, INVALID_PARAMETER, RATE_LIMIT, PARSE, NO_OFFLINE_DATA }

    public final Type type;

    public TriviaException(Type type, Throwable cause) {
        super(type.name(), cause);
        this.type = type;
    }

    public TriviaException(Type type) {
        this(type, null);
    }
}
