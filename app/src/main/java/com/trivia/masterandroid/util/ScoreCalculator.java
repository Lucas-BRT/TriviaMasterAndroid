package com.trivia.masterandroid.util;

/**
 * Score per correct answer = base points by difficulty + speed bonus.
 * Harder questions are worth more; answering faster adds up to 100% of the base.
 * Wrong answers and timeouts score zero (no negative points, keeps the game friendly).
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static int basePoints(String difficulty) {
        if ("hard".equals(difficulty)) {
            return 30;
        }
        if ("medium".equals(difficulty)) {
            return 20;
        }
        return 10;
    }

    public static int points(String difficulty, boolean correct, long timeLeftMs, long totalMs) {
        if (!correct || totalMs <= 0) {
            return 0;
        }
        int base = basePoints(difficulty);
        double ratio = Math.max(0, Math.min(1.0, (double) timeLeftMs / totalMs));
        return base + (int) Math.round(base * ratio);
    }
}
