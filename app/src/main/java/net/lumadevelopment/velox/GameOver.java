package net.lumadevelopment.velox;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class dedicated to handling the GameOver screen. Realistically could've
 * been included in the Game class, but I think this is cleaner.
 */
public class GameOver {

    public static final String LOG_TAG = GameOver.class.getSimpleName();

    // Passed in from Game
    private final Velox main;
    private final boolean[] correctlyAnswered;
    private final long gameStart;

    // UI elements
    private TextView gameTime;
    private TextView score;
    private Button goAgainButton;

    public GameOver(Velox main, boolean[] correctlyAnswered, long gameStart) {

        this.main = main;
        this.correctlyAnswered = correctlyAnswered;
        this.gameStart = gameStart;

        // When GameOver is initialized, it's because the game is over, so
        // there's no need to wait to run()
        run();

    }

    /**
     * Calculate end-game statistics and display them. Offer for the player
     * to play again, and initiate the next game if they choose to play again.
     */
    public void run() {

        Log.d(LOG_TAG, "run() in " + LOG_TAG + " called!");

        long gameEnd = System.currentTimeMillis();

        // Convert from ms to seconds
        double gameTimeInS = (gameEnd - gameStart) / 1000.0;

        Log.d(LOG_TAG, "Precise game time in seconds: " + gameTimeInS);

        // Round by cutting off everything after two decimal places
        double roundedGameTimeInS = ((int) (gameTimeInS * 100))/ 100.0;

        int points = 0;

        // 1 point for each question answered correctly
        for (boolean questionAnsweredCorrectly : correctlyAnswered) {
            if (questionAnsweredCorrectly) {
                points++;
            }
        }

        // pointsPossible is equivalent to Config.NUM_OF_PROBLEMS
        int pointsPossible = correctlyAnswered.length;

        Log.d(LOG_TAG, "GAME STATS | Game time: " + roundedGameTimeInS + "s, Score: " + points + "/" + pointsPossible);

        int finalPoints = points;
        main.runOnUiThread(() -> {

            main.setContentView(R.layout.game_over);
            gameTime = main.findViewById(R.id.gameTime);
            score = main.findViewById(R.id.score);
            goAgainButton = main.findViewById(R.id.goAgainButton);

            // Set text for UI elements
            gameTime.setText(Config.GAME_TIME_TEXT_PREFIX + roundedGameTimeInS + "s");
            score.setText(Config.SCORE_TEXT_PREFIX + finalPoints + "/" + pointsPossible);

            goAgainButton.setOnClickListener(v -> {

                Log.d(LOG_TAG, "Play again button pressed!");

                // Start the countdown again, which will create
                // another Game object.
                main.countdown();

            });

        });

        Log.d(LOG_TAG, "Game over UI successfully shown!");

    }

}
