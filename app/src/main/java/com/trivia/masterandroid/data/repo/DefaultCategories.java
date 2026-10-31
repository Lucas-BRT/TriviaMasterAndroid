package com.trivia.masterandroid.data.repo;

import com.trivia.masterandroid.model.Category;

import java.util.ArrayList;
import java.util.List;

/** Offline fallback; ids match the Open Trivia DB categories. */
final class DefaultCategories {

    private DefaultCategories() {
    }

    static List<Category> get() {
        List<Category> list = new ArrayList<>();
        list.add(new Category(9, "General Knowledge"));
        list.add(new Category(17, "Science & Nature"));
        list.add(new Category(18, "Science: Computers"));
        list.add(new Category(21, "Sports"));
        list.add(new Category(22, "Geography"));
        list.add(new Category(23, "History"));
        return list;
    }
}
