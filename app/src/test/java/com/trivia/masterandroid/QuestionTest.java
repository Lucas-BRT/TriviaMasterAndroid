package com.trivia.masterandroid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.trivia.masterandroid.model.Question;

import org.junit.Test;

import java.util.Arrays;

public class QuestionTest {

    private Question sample() {
        return new Question("Art", "easy", "Grey?", "Grey", Arrays.asList("Black", "White", "Brown"));
    }

    @Test
    public void optionsContainCorrectAndIncorrectAnswers() {
        Question q = sample();
        assertEquals(4, q.options.size());
        assertTrue(q.options.contains("Grey"));
        assertTrue(q.options.containsAll(Arrays.asList("Black", "White", "Brown")));
    }

    @Test
    public void isCorrectMatchesOnlyTheRightAnswer() {
        Question q = sample();
        assertTrue(q.isCorrect("Grey"));
        assertFalse(q.isCorrect("Black"));
    }
}
