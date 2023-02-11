package net.lumadevelopment.velox;

public class Config {

    // Numeric variables
    public static final int MAX_NUMBER = 30; // max number any solution can reach
    public static final int NUM_OF_PROBLEMS = 15; // how many problems in the game
    public static final int TIME_PER_PROBLEM_IN_MS = 3000; // the amount of time available to complete each problem
    public static final long TICKS_PER_SECOND = 30; // how many times Game.update() is called per second
    public static final int COUNTDOWN_TIME_IN_SECONDS = 5; // how long the countdown screen will display before the game starts

    // UI Text variables
    public static final String GAME_TIME_TEXT_PREFIX = "Game Time: ";
    public static final String SCORE_TEXT_PREFIX = "Score: ";

}
