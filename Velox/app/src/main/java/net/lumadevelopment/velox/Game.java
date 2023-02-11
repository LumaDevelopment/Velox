package net.lumadevelopment.velox;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Game object for Velox. Can be instantiated multiple times for replays.
 */
public class Game {

    // LOG_TAG changes if class name gets refactored
    public static final String LOG_TAG = Game.class.getSimpleName();

    /**
     * We use the Velox class to access the UI thread and
     * UI elements.
     */
    private Velox main;

    /**
     * The Timer that runs the update() TimerTask.
     * Stored so we can cancel() and purge() it when we kill()
     * a Game object.
     */
    private Timer timer;

    /**
     * Assigned to Config.NUM_OF_PROBLEMS
     */
    private int numOfProblems;

    /**
     * Array of problems the game will use. Generated
     * and assigned to this array in init()
     *
     * size = numOfProblems
     */
    private Problem[] problems;

    /**
     * Array of booleans, each one corresponding to a
     * problem. The value stores whether or not the
     * user solved the problem.
     *
     * size = numOfProblems
     */
    private boolean[] correctlyAnswered;

    /**
     * Whether init() has ran successfully or not. Necessary
     * because init() is ran concurrently while the countdown
     * is going. In the microscopic chance Game is not
     * initialized before the countdown is up, an error
     * will be thrown.
     */
    private boolean ready;

    /**
     * When the game was started, used for the GameOver screen.
     */
    private long gameStart;

    private int currentProblemIndex;

    /**
     * When the current problem we're on is started. Assigned with
     * System.currentTimeMillis(). Used for the progress bar and
     * for switching to the next problem when the current one's
     * time is up.
     */
    private long thisProblemStarted;

    /**
     * The last answer the user gave. Obviously we check this
     * against the solution and display it in the UI.
     */
    private String lastAnswer;

    // UI Elements
    private TextView equation;
    private ProgressBar progressBar;
    private TextView lastAnswerTextView;

    public Game(Velox main) {

        this.ready = false;

        this.main = main;
        this.timer = new Timer();
        this.numOfProblems = Config.NUM_OF_PROBLEMS;
        this.problems = new Problem[numOfProblems];
        this.correctlyAnswered = new boolean[numOfProblems];

        this.currentProblemIndex = 0;
        this.lastAnswer = "N/A";

    }

    /**
     * Initializes the Game object with problems.
     */
    public void init() {

        Log.d(LOG_TAG, "init() called in " + LOG_TAG);
        long startTime = System.currentTimeMillis();

        ProblemGenerator generator = new ProblemGenerator();

        // Repeat generation for ever problem we need.
        for (int i = 0; i < numOfProblems; i++) {

            Problem generatedProblem = generator.generateProblem();
            problems[i] = generatedProblem;

            // Increases init() run time from 0 -> 1 ms on test device :(
            Log.v(LOG_TAG, "Problem " + i + ": " + generatedProblem + " = " + generatedProblem.getSolution());

        }

        // Let the Velox class know that run() can be called
        ready = true;

        long endTime =  System.currentTimeMillis();
        long initializationTime = endTime - startTime;

        Log.d(LOG_TAG, LOG_TAG + " initialized in " + initializationTime + " ms.");

    }

    /**
     * Start the Game. Pull up the problem.xml layout, assign our time variables
     * for the first time, and start the update() TimerTask.
     */
    public void run() {

        if (!ready) {

            // This happens if init() does not finish before the countdown
            // finishes. Incredibly unlikely, but it's good to be prepared.
            Log.e(LOG_TAG, LOG_TAG + " not ready, cannot run!");
            return;

        }

        Log.d(LOG_TAG, "run() called on Thread " + Thread.currentThread().getName());

        // Android is a stickler about UI threads, so we do all of our UI
        //work in main.runOnUiThread()
        main.runOnUiThread(() -> {
            main.setContentView(R.layout.problem);
            this.equation = main.findViewById(R.id.equation);
            this.progressBar = main.findViewById(R.id.progressBar);
            this.lastAnswerTextView = main.findViewById(R.id.lastAnswer);
            progressBar.setMax(Config.TIME_PER_PROBLEM_IN_MS);
        });

        Log.d(LOG_TAG, "Problem UI successfully initialized!");

        gameStart = System.currentTimeMillis();
        thisProblemStarted = System.currentTimeMillis();

        // update() TimerTask.
        // period = (1000 / ticks per second) so the task runs
        // (ticks per second) times per second
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, 1000 / Config.TICKS_PER_SECOND);

        Log.d(LOG_TAG, "update() TimerTask dispatched, run() concluding.");

    }

    /**
     * Responsible for keeping track of time, calling goToNextProblem()
     * where necessary, and updating the UI
     */
    public void update() {

        int timeOnQuestionInMs = Math.toIntExact(System.currentTimeMillis() - thisProblemStarted);

        // Check if the time for this question is over
        if (timeOnQuestionInMs > Config.TIME_PER_PROBLEM_IN_MS) {

            Log.d(LOG_TAG, "Time for problem " + (currentProblemIndex + 1) + " is up, moving on to problem " + (currentProblemIndex + 2));

            // If the time period for this problem is over and the user
            // still hasn't submitted the correct answer, call
            // goToNextProblem() with a false parameter.
            goToNextProblem(false);

            // End of game condition
            if(currentProblemIndex == numOfProblems) {
                Log.d(LOG_TAG, "Game has ended, not continuing update()");
                return;
            }

            // Reset progress bar
            timeOnQuestionInMs = 0;

        }

        int finalTimeOnQuestionInMs = timeOnQuestionInMs;

        // Update UI
        main.runOnUiThread(() -> {

            Problem p = problems[currentProblemIndex];
            equation.setText(p.toString());

            // progress is scaled to Config.TIME_PER_PROBLEM_IN_MS
            progressBar.setProgress(finalTimeOnQuestionInMs);

            lastAnswerTextView.setText(lastAnswer);

        });

    }

    /**
     * Records whether or not the user answered the problem correctly,
     * advances the problem index, and resets the problem start clock.
     * Ends the game if we've done all questions.
     * @param answeredCorrectly True if the user answered correctly.
     *                          False if the user ran out of time.
     */
    public void goToNextProblem(boolean answeredCorrectly) {

        String log = "Problem " + (currentProblemIndex + 1) + " was answered ";

        if(!answeredCorrectly) {
            log += "in";
        }

        log += "correctly";
        Log.d(LOG_TAG, log);

        correctlyAnswered[currentProblemIndex] = answeredCorrectly;
        currentProblemIndex++;
        thisProblemStarted = System.currentTimeMillis();

        if(currentProblemIndex == numOfProblems) {

            Log.d(LOG_TAG, "Final index of problems reached, game over!");

            // All questions have been done
            gameOver();

        }

    }

    /**
     * Called when the user has provided a new answer.
     * Updates the lastAnswer instance variable and calls
     * goToNextProblem(true) if the user got the answer right.
     * @param answer The user's answer
     */
    public void newAnswer(int answer) {

        Log.d(LOG_TAG, "User has submitted new answer: " + answer);

        lastAnswer = String.valueOf(answer);

        if (answer == problems[currentProblemIndex].getSolution()) {

            Log.d(LOG_TAG, "User submitted answer correct!");
            goToNextProblem(true);

        } else {

            Log.d(LOG_TAG, "User submitted answer incorrect.");

        }

    }

    /**
     * End of game. Kills this game object and launches the GameOver screen.
     */
    public void gameOver() {

        kill();

        // Pass in main for UI accessibility
        // Pass in the other variables for end of game stats
        new GameOver(main, correctlyAnswered, gameStart);

        Log.d(LOG_TAG, LOG_TAG + " killed, passing over to " + GameOver.LOG_TAG);

    }

    /**
     * Kill all threads.
     */
    public void kill() {

        Log.d(LOG_TAG, "kill() called!");

        timer.cancel();
        timer.purge();
        // TODO kill inference process

    }

}
