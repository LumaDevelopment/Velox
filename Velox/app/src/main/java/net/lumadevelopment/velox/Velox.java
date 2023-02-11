package net.lumadevelopment.velox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The main launching point for the app. Deals with the activity_main.xml layout,
 * countdown to the game, concurrent preparation of the Game object, and the
 * launching of the Game.
 */
public class Velox extends AppCompatActivity {

    public static final String LOG_TAG = Velox.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.goButton);

        button.setOnClickListener(view -> {

            Log.d(LOG_TAG, "Go button pressed!");
            countdown();

        });

    }

    /**
     * The true entry point for new games, used by the original launch
     * and by new games prompted from the GameOver screen.
     *
     * Starts a TimerTask to deal with the countdown and concurrently
     * launches the new Game object's init() function.
     */
    public void countdown() {

        Log.d(LOG_TAG, "countdown() called, bringing up countdown content view...");

        // Pull up the countdown screen
        runOnUiThread(() -> {

            setContentView(R.layout.countdown);
            TextView countdownNumber = findViewById(R.id.countdownNum);

            // Initially countdown value is the number of seconds in the countdown time
            countdownNumber.setText("" + Config.COUNTDOWN_TIME_IN_SECONDS);

        });

        // Initialize the objects we'll need
        Timer timer = new Timer();
        final Game game = new Game(this);

        // Countdown TimerTask, waits one second, runs for
        // the first time, then runs every second after that
        // until cancelled.
        timer.scheduleAtFixedRate(new TimerTask() {

            // Due to the 1 second delay in the first TimerTask run, the initial count
            // needs to be one second lower than the countdown time
            int count = Config.COUNTDOWN_TIME_IN_SECONDS - 1;

            // Having the make a countdownNumber variable twice hurts my soul,
            // but it works out best this way.
            TextView countdownNumber = findViewById(R.id.countdownNum);

            @Override
            public void run() {

                Log.d(LOG_TAG, "Countdown timer task run(), count = " + count);

                runOnUiThread(() -> {

                    if (count == 0) {

                        // The clock strikes 0
                        Log.d(LOG_TAG, "Count is 0! Cancelling timers and running game!");

                        timer.cancel();
                        timer.purge();

                        // Start the game!
                        game.run();

                        return;

                    }

                    // Update UI with new countdown number and decrement the count variable by 1
                    countdownNumber.setText("" + count);
                    count--;

                });

            }

        }, 1000, 1000);

        // Initialize Game object while countdown is running
        new Thread(() -> game.init()).start();

    }

}