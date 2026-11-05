package com.trivia.masterandroid.model;

public class MatchRecord implements java.io.Serializable {

    public long id;
    public long playerId;
    public String playerName;
    public String category;
    public String difficulty;
    public int total;
    public int correct;
    public int score;
    public boolean offline;
    public long playedAt;
}
