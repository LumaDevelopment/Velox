package net.lumadevelopment.velox;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Optional;

public class IntFromPredictionTest {
    @Test
    public void intFromPrediction_isCorrect() {

        assertEquals(11, (int) SpeechManager.intFromPrediction("11"));
        assertEquals(11, (int) SpeechManager.intFromPrediction("11:00"));
        assertEquals(11, (int) SpeechManager.intFromPrediction("11th"));
        assertEquals(11, (int) SpeechManager.intFromPrediction("eleven"));

    }
}