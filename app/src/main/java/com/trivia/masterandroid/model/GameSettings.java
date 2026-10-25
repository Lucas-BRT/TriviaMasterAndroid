package com.trivia.masterandroid.model;

import java.io.Serializable;

public class GameSettings implements Serializable {

    /** Seconds allowed per question (assignment: between 10 and 60). */
    public static final int TIME_PER_QUESTION_SECONDS = 20;

    public final String playerName;
    public final long playerId;
    public final int categoryId;
    public final String categoryName;
    /** "easy", "medium", "hard" or empty for any. */
    public final String difficulty;
    public final int amount;

    public GameSettings(String playerName, long playerId, int categoryId, String categoryName,
                        String difficulty, int amount) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.difficulty = difficulty;
        this.amount = amount;
    }
}
