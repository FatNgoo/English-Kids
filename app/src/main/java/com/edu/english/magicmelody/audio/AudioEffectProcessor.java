package com.edu.english.magicmelody.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 🎸 Audio Effect Processor
 * 
 * Handles audio effects and sound synthesis:
 * - Tone generation
 * - Simple effects (reverb simulation, echo)
 * - Dynamic pitch adjustment
 */
public class AudioEffectProcessor {
    
    private static final String TAG = "AudioEffectProcessor";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 EFFECT TYPES
    // ═══════════════════════════════════════════════════════════════
    
    public enum EffectType {
        NONE,
        ECHO,
        REVERB,
        PITCH_SHIFT,
        TREMOLO,
        VIBRATO
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private float volume = 1.0f;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public AudioEffectProcessor() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 TONE GENERATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Generate and play a pure tone
     */
    public void playTone(float frequency, int durationMs) {
        playTone(frequency, durationMs, WaveType.SINE);
    }
    
    /**
     * Generate and play a tone with specific wave type
     */
    public void playTone(float frequency, int durationMs, WaveType waveType) {
        new Thread(() -> {
            try {
                int numSamples = (int) ((durationMs / 1000.0) * SAMPLE_RATE);
                short[] samples = generateWave(frequency, numSamples, waveType);
                
                // Apply envelope for smooth attack/release
                applyEnvelope(samples, 0.01f, 0.05f);
                
                playBuffer(samples);
            } catch (Exception e) {
                Log.e(TAG, "Error playing tone", e);
            }
        }).start();
    }
    
    public enum WaveType {
        SINE,
        SQUARE,
        TRIANGLE,
        SAWTOOTH
    }
    
    /**
     * Generate wave samples
     */
    private short[] generateWave(float frequency, int numSamples, WaveType type) {
        short[] samples = new short[numSamples];
        double period = SAMPLE_RATE / frequency;
        
        for (int i = 0; i < numSamples; i++) {
            double t = i / (double) SAMPLE_RATE;
            double value;
            
            switch (type) {
                case SINE:
                    value = Math.sin(2 * Math.PI * frequency * t);
                    break;
                    
                case SQUARE:
                    value = Math.sin(2 * Math.PI * frequency * t) >= 0 ? 1 : -1;
                    break;
                    
                case TRIANGLE:
                    double phase = (i % period) / period;
                    value = phase < 0.5 ? (4 * phase - 1) : (3 - 4 * phase);
                    break;
                    
                case SAWTOOTH:
                    value = 2 * ((i % period) / period) - 1;
                    break;
                    
                default:
                    value = 0;
            }
            
            samples[i] = (short) (value * Short.MAX_VALUE * volume);
        }
        
        return samples;
    }
    
    /**
     * Apply ADSR-like envelope
     */
    private void applyEnvelope(short[] samples, float attackRatio, float releaseRatio) {
        int attackSamples = (int) (samples.length * attackRatio);
        int releaseSamples = (int) (samples.length * releaseRatio);
        int releaseStart = samples.length - releaseSamples;
        
        // Attack phase
        for (int i = 0; i < attackSamples; i++) {
            float multiplier = (float) i / attackSamples;
            samples[i] = (short) (samples[i] * multiplier);
        }
        
        // Release phase
        for (int i = releaseStart; i < samples.length; i++) {
            float multiplier = (float) (samples.length - i) / releaseSamples;
            samples[i] = (short) (samples[i] * multiplier);
        }
    }
    
    /**
     * Play audio buffer
     */
    private void playBuffer(short[] samples) {
        int bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        AudioFormat format = new AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(CHANNEL_CONFIG)
            .setEncoding(AUDIO_FORMAT)
            .build();
        
        audioTrack = new AudioTrack(
            attributes, format, bufferSize,
            AudioTrack.MODE_STREAM, 0);
        
        audioTrack.play();
        isPlaying = true;
        
        audioTrack.write(samples, 0, samples.length);
        
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        isPlaying = false;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎼 MUSICAL NOTES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get frequency for a musical note
     * @param note Note index (0=C4, 1=C#4, 2=D4, etc.)
     * @param octaveOffset Octave offset from 4
     */
    public float getNoteFrequency(int note, int octaveOffset) {
        // A4 = 440Hz, using equal temperament
        // Note 0 = C4, Note 9 = A4
        float a4 = 440.0f;
        int semitonesFromA4 = note - 9 + (octaveOffset * 12);
        return (float) (a4 * Math.pow(2, semitonesFromA4 / 12.0));
    }
    
    /**
     * Play a musical note
     */
    public void playNote(int note, int octaveOffset, int durationMs) {
        float frequency = getNoteFrequency(note, octaveOffset);
        playTone(frequency, durationMs, WaveType.SINE);
    }
    
    /**
     * Play a chord
     */
    public void playChord(int[] notes, int octaveOffset, int durationMs) {
        // Mix multiple frequencies
        new Thread(() -> {
            try {
                int numSamples = (int) ((durationMs / 1000.0) * SAMPLE_RATE);
                short[] mixedSamples = new short[numSamples];
                
                for (int note : notes) {
                    float freq = getNoteFrequency(note, octaveOffset);
                    short[] noteSamples = generateWave(freq, numSamples, WaveType.SINE);
                    
                    // Mix (with normalization)
                    for (int i = 0; i < numSamples; i++) {
                        int mixed = mixedSamples[i] + (noteSamples[i] / notes.length);
                        mixedSamples[i] = (short) Math.max(Short.MIN_VALUE, 
                                                          Math.min(Short.MAX_VALUE, mixed));
                    }
                }
                
                applyEnvelope(mixedSamples, 0.02f, 0.1f);
                playBuffer(mixedSamples);
            } catch (Exception e) {
                Log.e(TAG, "Error playing chord", e);
            }
        }).start();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎸 EFFECTS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Apply echo effect to samples
     */
    public short[] applyEcho(short[] samples, float delay, float decay) {
        int delaySamples = (int) (delay * SAMPLE_RATE);
        short[] result = new short[samples.length + delaySamples];
        
        // Copy original
        System.arraycopy(samples, 0, result, 0, samples.length);
        
        // Add echo
        for (int i = delaySamples; i < result.length; i++) {
            int originalIndex = i - delaySamples;
            if (originalIndex < samples.length) {
                int mixed = result[i] + (int)(samples[originalIndex] * decay);
                result[i] = (short) Math.max(Short.MIN_VALUE, 
                                             Math.min(Short.MAX_VALUE, mixed));
            }
        }
        
        return result;
    }
    
    /**
     * Apply simple reverb effect
     */
    public short[] applyReverb(short[] samples, float roomSize) {
        // Simulate reverb with multiple echoes
        short[] result = samples.clone();
        float[] delays = {0.03f, 0.05f, 0.07f, 0.11f};
        float[] decays = {0.6f, 0.4f, 0.25f, 0.15f};
        
        for (int i = 0; i < delays.length; i++) {
            float delay = delays[i] * roomSize;
            result = applyEcho(result, delay, decays[i]);
        }
        
        return result;
    }
    
    /**
     * Apply pitch shift (simple resampling)
     */
    public short[] applyPitchShift(short[] samples, float pitchFactor) {
        int newLength = (int) (samples.length / pitchFactor);
        short[] result = new short[newLength];
        
        for (int i = 0; i < newLength; i++) {
            float sourceIndex = i * pitchFactor;
            int index1 = (int) sourceIndex;
            int index2 = Math.min(index1 + 1, samples.length - 1);
            float frac = sourceIndex - index1;
            
            // Linear interpolation
            result[i] = (short) (samples[index1] * (1 - frac) + samples[index2] * frac);
        }
        
        return result;
    }
    
    /**
     * Apply tremolo effect
     */
    public short[] applyTremolo(short[] samples, float rate, float depth) {
        short[] result = new short[samples.length];
        
        for (int i = 0; i < samples.length; i++) {
            float t = (float) i / SAMPLE_RATE;
            float modulation = 1.0f - depth * 0.5f * (1 + (float) Math.sin(2 * Math.PI * rate * t));
            result[i] = (short) (samples[i] * modulation);
        }
        
        return result;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔊 FEEDBACK SOUNDS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play success sound
     */
    public void playSuccessSound() {
        // Rising arpeggio: C-E-G-C
        handler.postDelayed(() -> playNote(0, 0, 100), 0);     // C4
        handler.postDelayed(() -> playNote(4, 0, 100), 100);   // E4
        handler.postDelayed(() -> playNote(7, 0, 100), 200);   // G4
        handler.postDelayed(() -> playNote(0, 1, 200), 300);   // C5
    }
    
    /**
     * Play error sound
     */
    public void playErrorSound() {
        // Descending: E-D-C
        handler.postDelayed(() -> playNote(4, 0, 150), 0);     // E4
        handler.postDelayed(() -> playNote(2, 0, 150), 150);   // D4
        handler.postDelayed(() -> playNote(0, 0, 200), 300);   // C4
    }
    
    /**
     * Play countdown beep
     */
    public void playCountdownBeep(boolean isFinal) {
        if (isFinal) {
            playTone(880, 300, WaveType.SINE);  // A5 - higher pitch for "GO"
        } else {
            playTone(440, 100, WaveType.SINE);  // A4
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ SETTINGS
    // ═══════════════════════════════════════════════════════════════
    
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }
    
    public float getVolume() {
        return volume;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void stop() {
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping audio", e);
            }
            audioTrack = null;
        }
        isPlaying = false;
    }
    
    public void release() {
        stop();
        handler.removeCallbacksAndMessages(null);
    }
}
