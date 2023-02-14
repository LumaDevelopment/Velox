package net.lumadevelopment.velox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
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

    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public Velox() {

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                countdown();

            } else {

                showPermissionDeniedDialog();

            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.goButton);

        button.setOnClickListener(view -> {

            Log.d(LOG_TAG, "Go button pressed!");
            permissionLayer();

        });

    }

    /**
     * Check for appropriate permission before moving on to countdown.
     */
    public void permissionLayer() {

        Log.d(LOG_TAG, "Entering permissions layer...");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            // If we already have the permission
            Log.d(LOG_TAG, "We have permission to use the microphone, passing on to countdown!");
            countdown();

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {

            // If we should explain to the user why we need the permission
            Log.d(LOG_TAG, "Calling permission request dialog from permission layer!");
            showPermissionRequestDialog();

        } else {

            // If we can simply ask for the permission
            Log.d(LOG_TAG, "Request permission directly from permission layer!");
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);

        }

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
            final TextView countdownNumber = findViewById(R.id.countdownNum);

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

    /**
     * Creates and shows a dialog to the user that explains what
     * we need microphone permission for, and gives the user the
     * option to either grant the microphone permission or to
     * deny us the microphone permission.
     */
    public void showPermissionRequestDialog() {

        Log.d(LOG_TAG, "Launching permission request dialog!");

        AlertDialog.Builder permRequestDialog = new AlertDialog.Builder(this);

        permRequestDialog.setMessage(Config.PERMISSION_REQUEST_MSG).setTitle(Config.PERMISSION_REQUEST_TITLE);

        permRequestDialog.setPositiveButton("OK", (dialogInterface, i) -> {

            Log.d(LOG_TAG, "Request permission from permission request dialog!");
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);

        });

        permRequestDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {

            Log.d(LOG_TAG, "Permission request dialog cancelled, calling permission denied dialog.");
            showPermissionDeniedDialog();

        });

        permRequestDialog.create().show();

        Log.d(LOG_TAG, "Permission request dialog shown!");

    }

    /**
     * Creates and shows a dialog to the user that explains
     * that the app cannot work without the microphone
     * permission. The dialog gives the user the option
     * to either enable the microphone permission or to
     * just deny it.
     */
    public void showPermissionDeniedDialog() {

        Log.d(LOG_TAG, "Launching permission denied dialog.");

        AlertDialog.Builder permDeniedDialog = new AlertDialog.Builder(this);

        permDeniedDialog.setMessage(Config.PERMISSION_DENIED_MSG).setTitle(Config.PERMISSION_DENIED_TITLE);

        permDeniedDialog.setPositiveButton("Give", (dialogInterface, i) -> {

            Log.d(LOG_TAG, "Calling permission request dialog from permission denied dialog.");
            showPermissionRequestDialog();

        });

        permDeniedDialog.setNegativeButton("Cancel", (dialogInterface, i) -> Log.d(LOG_TAG, "Permission denied dialog closed."));

        permDeniedDialog.create().show();

        Log.d(LOG_TAG, "Permission denied dialog shown.");

    }

}