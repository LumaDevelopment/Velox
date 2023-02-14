package net.lumadevelopment.velox;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import pl.allegro.finance.tradukisto.ValueConverters;

/**
 * SpeechManager handles everything to do with user input by
 * audio. This includes starting recording, transcribing audio,
 * interpreting that transcription into a number answer,
 * and notifying the Game object of new answers.
 */
public class SpeechManager implements RecognitionListener {

    // Debug logging
    public static final String LOG_TAG = SpeechManager.class.getSimpleName();

    // SpeechRecognizer needs the application context frequently for
    // permissions and other functions that verify we're allowed to
    // capture audio.
    private final Velox main;

    // For game.newAnswer()
    private final Game game;

    // Android's object for interpreting speech
    private SpeechRecognizer recognizer;

    // Telling the SpeechRecognizer what we want to do
    // (record audio) and how we want to do it (offline
    // where possible, using configurable Locale, etc.)

    /**
     * Tells the SpeechRecognizer what we want to do
     * (record audio) and how we want to do it (offline
     * where possible, using configurable Locale, etc.)
     */
    private Intent speechRecognizerIntent;

    /**
     * The number words for every number from 0 (inclusive)
     * to Config.MAX_NUMBER (inclusive), just in case the
     * phone predicts a number word instead of an actual
     * number for the audio.
     */
    public static HashMap<Integer, String> numberWords;

    /**
     * Marks whether SpeechManager is healthy.
     */
    private boolean ready;

    public SpeechManager(Velox main, Game game) {

        this.ready = false;
        this.main = main;
        this.game = game;
        this.numberWords = new HashMap<>();

        init();

    }

    // Getter/Setter method

    public boolean isReady() {
        return ready;
    }

    /**
     * Fill numberWords map by looping from 0 (inclusive)
     * to Config.MAX_NUMBER (inclusive) and getting it's
     * equivalent number word from the ValueConverters class.
     */
    public void fillNumberWordsMap() {

        // The only thing from an external library in this project.
        // Not my first guess for external library usage.
        // Better than hardcoding 0 -> 30.
        ValueConverters intConverter = ValueConverters.getByLocaleOrDefault(Config.LOCALE, ValueConverters.ENGLISH_INTEGER);

        for (int i = 0; i <= Config.MAX_NUMBER; i++) {

            numberWords.put(i, intConverter.asWords(i));

        }

        Log.d(LOG_TAG, "Filled the number words map with " + numberWords.size() + " pairs.");

    }

    /**
     * Make the call to fill the numberWords map, create the speech recognizer intent,
     * check if speech recognition is available, create the speech recognizer, and set
     * this class as the listener for the speech recognizer.
     */
    public void init() {

        // First true initialization task
        fillNumberWordsMap();

        // We want to recognize speech
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Partial results can sometimes pick up the first number in a two-digit number.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        // I want the app to work offline, I'm not a fan of online-only mobile games.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        // We're not trying to do anything from the web here, just the words as they are.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Configurable locale for answers
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Config.LOCALE);

        if (!SpeechRecognizer.isRecognitionAvailable(main)) {

            Log.e(LOG_TAG, "Speech recognition unavailable!");
            return;

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(main)) {

            // Certain devices are qualified for on device speech recognition.
            // However, with this OS requirements and individual device
            // requirements, this is pretty rare. Where it's available, it's
            // preferred.
            Log.d(LOG_TAG, "Using on device speech recognition!");
            this.recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(main);

        } else {

            // Give a reason why on device speech recognition failed.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {

                Log.d(LOG_TAG, "Android OS too old for on-device speech recognition, SDK_INT = " + Build.VERSION.SDK_INT);

            } else {

                Log.d(LOG_TAG, "isOnDeviceRecognitionAvailable() returned false.");

            }

            this.recognizer = SpeechRecognizer.createSpeechRecognizer(main);

            Log.d(LOG_TAG, "Successfully using regular speech recognizer instead!");

        }

        // This class receives all information from the speech recognizer,
        // like what words are predicted, error codes, etc.
        this.recognizer.setRecognitionListener(this);

    }

    /**
     * Start listening. Separate from init() because init() runs
     * at countdown and we don't want speech interpretation before
     * the game actually starts.
     */
    public void run() {

        Log.d(LOG_TAG, "Attempting to start speech recognition!");
        this.recognizer.startListening(speechRecognizerIntent);

        this.ready = true;

    }

    /**
     * Called when the speech recognizer stops due to an error. Attempts
     * to start the recognizer back up again. The most common error is
     * error code 7: 'no match.' This is typically because the user is
     * not saying anything.
     * @param errorCode Error code thrown
     */
    @Override
    public void onError(int errorCode) {

        Log.d(LOG_TAG, "SpeechRecognizer threw error code " + errorCode + ", user probably not speaking, attempting to keep listening!");
        recognizer.startListening(speechRecognizerIntent);

    }

    /**
     * Called when the speech recognizer determines the user has stopped talking
     * and makes final predictions. Loops through all predictions (most confident
     * first) and tries to convert that prediction to a number. If successful,
     * tell the Game object. Either way, continue listening.
     * @param bundle Results from the prediction
     */
    @Override
    public void onResults(Bundle bundle) {

        Log.d(LOG_TAG, "Full recognition results obtained!");

        List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        for (int i = 0; i < results.size(); i++) {

            // Try to see if the prediction corresponds to an Integer
            Integer convertedPrediction = intFromPrediction(results.get(i));

            if (convertedPrediction != null) {

                // If so, new answer, no need ot keep looking
                game.newAnswer(convertedPrediction);
                break;

            }

        }

        // Listen for next answer
        recognizer.startListening(speechRecognizerIntent);

    }

    /**
     * Tries to extract an integer from a String prediction such as
     * "11", "11:00", "11th", "eleven", etc.
     * @param prediction Prediction from speech recognizer
     * @return Integer if the prediction contains a number in a
     * format we recognize and are prepared for, null if not.
     */
    public static Integer intFromPrediction(String prediction) {

        try {

            // Approach 1: is the prediction literally just the number?
            int straightConvert = Integer.parseInt(prediction);

            Log.d(LOG_TAG, "Prediction (" + prediction + ") was a number: " + straightConvert);
            return straightConvert;

        } catch (Exception e) {

            // prediction is not the number exactly

        }

        prediction = prediction.toLowerCase();

        // Start from max number because if you start from lower numbers
        // the startWith() conditions interpret double digits numbers as
        // just the first number
        for (int i = Config.MAX_NUMBER; i >= 0; i--) {

            String numberText = "" + i;

            // Approach 2: Does the prediction contain the number
            // and other characters?
            if(prediction.contains(numberText)) {

                Log.d(LOG_TAG, "Prediction (" + prediction + ") contained number: " + i);

                return i;

            }

            String numberWord = numberWords.get(i);

            if (numberWord == null) {

                // Should be impossible because numberWords are generated up to
                // and including MAX_NUMBER, but better safe than sorry
                Log.e(LOG_TAG, "Number word for " + i + " doesn't exist!");
                return null;

            }

            // Approach 3: Does the prediction contain, equal,
            // or start with the numberWord?
            if(prediction.equalsIgnoreCase(numberWord) ||
                    prediction.contains(numberWord)) {

                Log.d(LOG_TAG, "Prediction (" + prediction + ") contained word of number: " + i);

                return i;

            }

        }

        Log.d(LOG_TAG, "No number gathered from: " + prediction);
        return null;

    }

    /**
     * Kill the SpeechManager. Used when shutting down
     * one Game to start a new one.
     */
    public void kill() {
        ready = false;
        recognizer.cancel();
        recognizer.destroy();
    }

    // I don't use these methods, but they're required by the interface.

    @Override
    public void onReadyForSpeech(Bundle bundle) {

        // Ready to hear speech

    }

    @Override
    public void onBeginningOfSpeech() {

        // User has begun speaking

    }

    @Override
    public void onRmsChanged(float v) {

        // Audio sound level changed

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

        // Can be used to monitor if audio is being received

    }

    @Override
    public void onEndOfSpeech() {

        // User has stopped speaking

    }

    @Override
    public void onPartialResults(Bundle bundle) {

        // Can be used to get partial results while recognizing speech.

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

        // Future proofing from Android devs

    }

}
