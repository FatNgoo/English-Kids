package com.edu.english.magicmelody.core.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * ToneEngine - Fallback audio engine using AudioTrack sine wave generation
 * Used when SoundPool samples are not available
 * Generates pure sine wave tones for piano notes
 */
public class ToneEngine {
    
    private static final int SAMPLE_RATE = 44100;
    private static final int AMPLITUDE = 10000;
    
    // Piano note frequencies (C4 to B4)
    private static final double[] NOTE_FREQUENCIES = {
        261.63,  // C4
        293.66,  // D4
        329.63,  // E4
        349.23,  // F4
        392.00,  // G4
        440.00,  // A4
        493.88   // B4
    };
    
    // Extended frequencies for more octaves
    private static final double[] EXTENDED_FREQUENCIES = {
        130.81, 146.83, 164.81, 174.61, 196.00, 220.00, 246.94,  // C3-B3
        261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88,  // C4-B4
        523.25, 587.33, 659.26, 698.46, 783.99, 880.00, 987.77   // C5-B5
    };
    
    private AudioTrack currentTrack;
    private volatile boolean isPlaying = false;
    
    public ToneEngine() {
        // Constructor
    }
    
    /**
     * Play a note by index (0-6 for C-B)
     * @param noteIndex 0=C, 1=D, 2=E, 3=F, 4=G, 5=A, 6=B
     * @param durationMs Duration in milliseconds
     */
    public void playNote(int noteIndex, int durationMs) {
        if (noteIndex < 0 || noteIndex >= NOTE_FREQUENCIES.length) {
            return;
        }
        playFrequency(NOTE_FREQUENCIES[noteIndex], durationMs);
    }
    
    /**
     * Play a specific frequency
     * @param frequency Frequency in Hz
     * @param durationMs Duration in milliseconds
     */
    public void playFrequency(double frequency, int durationMs) {
        // Stop any currently playing tone
        stop();
        
        // Calculate buffer size
        int numSamples = (int) ((durationMs / 1000.0) * SAMPLE_RATE);
        short[] buffer = generateSineWave(frequency, numSamples);
        
        // Apply envelope to prevent clicks
        applyEnvelope(buffer);
        
        // Create AudioTrack
        int bufferSize = buffer.length * 2; // 2 bytes per short
        
        AudioTrack track = new AudioTrack.Builder()
            .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build();
        
        track.write(buffer, 0, buffer.length);
        
        currentTrack = track;
        isPlaying = true;
        
        // Play asynchronously
        new Thread(() -> {
            try {
                track.play();
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                // Interrupted
            } finally {
                track.stop();
                track.release();
                isPlaying = false;
            }
        }).start();
    }
    
    /**
     * Play a short melody (sequence of notes)
     * @param noteIndices Array of note indices
     * @param noteDurationMs Duration for each note
     * @param callback Callback when melody finishes
     */
    public void playMelody(int[] noteIndices, int noteDurationMs, Runnable callback) {
        new Thread(() -> {
            for (int noteIndex : noteIndices) {
                if (noteIndex >= 0 && noteIndex < NOTE_FREQUENCIES.length) {
                    playNoteBlocking(NOTE_FREQUENCIES[noteIndex], noteDurationMs);
                }
                try {
                    Thread.sleep(50); // Small gap between notes
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (callback != null) {
                callback.run();
            }
        }).start();
    }
    
    /**
     * Play note and block until finished
     */
    private void playNoteBlocking(double frequency, int durationMs) {
        int numSamples = (int) ((durationMs / 1000.0) * SAMPLE_RATE);
        short[] buffer = generateSineWave(frequency, numSamples);
        applyEnvelope(buffer);
        
        int minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );
        
        int bufferSize = Math.max(buffer.length * 2, minBufferSize);
        
        AudioTrack track = new AudioTrack.Builder()
            .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build();
        
        track.write(buffer, 0, buffer.length);
        track.play();
        
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            // Interrupted
        }
        
        track.stop();
        track.release();
    }
    
    /**
     * Generate sine wave samples
     */
    private short[] generateSineWave(double frequency, int numSamples) {
        short[] buffer = new short[numSamples];
        double twoPiF = 2 * Math.PI * frequency;
        
        for (int i = 0; i < numSamples; i++) {
            double time = (double) i / SAMPLE_RATE;
            buffer[i] = (short) (AMPLITUDE * Math.sin(twoPiF * time));
        }
        
        return buffer;
    }
    
    /**
     * Apply attack/decay envelope to prevent clicks
     */
    private void applyEnvelope(short[] buffer) {
        int attackSamples = Math.min(buffer.length / 10, SAMPLE_RATE / 50); // 20ms attack
        int decaySamples = Math.min(buffer.length / 5, SAMPLE_RATE / 20);   // 50ms decay
        
        // Attack
        for (int i = 0; i < attackSamples; i++) {
            float multiplier = (float) i / attackSamples;
            buffer[i] = (short) (buffer[i] * multiplier);
        }
        
        // Decay
        int decayStart = buffer.length - decaySamples;
        for (int i = 0; i < decaySamples; i++) {
            float multiplier = 1.0f - ((float) i / decaySamples);
            buffer[decayStart + i] = (short) (buffer[decayStart + i] * multiplier);
        }
    }
    
    /**
     * Stop current playback
     */
    public void stop() {
        if (currentTrack != null && isPlaying) {
            try {
                currentTrack.stop();
                currentTrack.release();
            } catch (Exception e) {
                // Ignore
            }
            currentTrack = null;
            isPlaying = false;
        }
    }
    
    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Get frequency for a note index
     */
    public static double getFrequency(int noteIndex) {
        if (noteIndex >= 0 && noteIndex < NOTE_FREQUENCIES.length) {
            return NOTE_FREQUENCIES[noteIndex];
        }
        return 440.0; // Default A4
    }
    
    /**
     * Release resources
     */
    public void release() {
        stop();
    }
}
