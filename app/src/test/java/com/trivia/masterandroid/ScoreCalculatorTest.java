package com.trivia.masterandroid;

import static org.junit.Assert.assertEquals;

import com.trivia.masterandroid.util.ScoreCalculator;

import org.junit.Test;

public class ScoreCalculatorTest {

    private static final long TOTAL = 20_000;

    @Test
    public void wrongAnswerScoresZero() {
        assertEquals(0, ScoreCalculator.points("hard", false, TOTAL, TOTAL));
    }

    @Test
    public void instantAnswerDoublesBase() {
        assertEquals(20, ScoreCalculator.points("easy", true, TOTAL, TOTAL));
        assertEquals(40, ScoreCalculator.points("medium", true, TOTAL, TOTAL));
        assertEquals(60, ScoreCalculator.points("hard", true, TOTAL, TOTAL));
    }

    @Test
    public void lastSecondAnswerGivesOnlyBase() {
        assertEquals(10, ScoreCalculator.points("easy", true, 0, TOTAL));
    }

    @Test
    public void halfTimeGivesHalfBonus() {
        assertEquals(30, ScoreCalculator.points("medium", true, TOTAL / 2, TOTAL));
    }

    @Test
    public void invalidTotalIsSafe() {
        assertEquals(0, ScoreCalculator.points("easy", true, 5_000, 0));
    }
}
