package com.edu.english.magicmelody.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 🎹 Note Audio Player
 * 
 * Specialized audio player for musical notes:
 * - 7 base notes (Do Re Mi Fa Sol La Si)
 * - Multiple octaves
 * - Pitch variation
 * - Harmonics and chords
 */
public class NoteAudioPlayer {
    
    private static final String TAG = "NoteAudioPlayer";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 NOTES
    // ═══════════════════════════════════════════════════════════════
    
    public enum Note {
        DO(0, "C", 261.63f),    // C4
        RE(1, "D", 293.66f),    // D4
        MI(2, "E", 329.63f),    // E4
        FA(3, "F", 349.23f),    // F4
        SOL(4, "G", 392.00f),   // G4
        LA(5, "A", 440.00f),    // A4
        SI(6, "B", 493.88f);    // B4
        
        private final int index;
        private final String letter;
        private final float frequency;
        
        Note(int index, String letter, float frequency) {
            this.index = index;
            this.letter = letter;
            this.frequency = frequency;
        }
        
        public int getIndex() { return index; }
        public String getLetter() { return letter; }
        public float getFrequency() { return frequency; }
        
        public static Note fromIndex(int index) {
            int normalized = ((index % 7) + 7) % 7;
            for (Note n : values()) {
                if (n.index == normalized) return n;
            }
            return DO;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private Context context;
    private SoundPool soundPool;
    private final Map<Note, Integer> noteSoundIds = new HashMap<>();
    private boolean isLoaded = false;
    
    // Volume
    private float volume = 1.0f;
    private boolean isMuted = false;
    
    // Sound folder
    private static final String NOTES_FOLDER = "magicmelody/sounds/notes/";
    
    // Octave settings
    private int baseOctave = 4;  // C4 = middle C
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface NotePlayListener {
        void onNoteStarted(Note note, int octave);
        void onNoteStopped(Note note);
    }
    
    private NotePlayListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    public NoteAudioPlayer(Context context) {
        this.context = context.getApplicationContext();
        initialize();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(NotePlayListener listener) {
        this.listener = listener;
    }
    
    private void initialize() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(7)  // One per note
            .setAudioAttributes(attributes)
            .build();
        
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            // Check if all loaded
            if (noteSoundIds.size() == Note.values().length) {
                isLoaded = true;
            }
        });
        
        loadNotes();
    }
    
    private void loadNotes() {
        for (Note note : Note.values()) {
            loadNote(note);
        }
    }
    
    private void loadNote(Note note) {
        String fileName = "note_" + note.name().toLowerCase() + ".ogg";
        String path = NOTES_FOLDER + fileName;
        
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(path);
            int soundId = soundPool.load(afd, 1);
            noteSoundIds.put(note, soundId);
            afd.close();
        } catch (IOException e) {
            Log.w(TAG, "Note file not found: " + fileName);
            // Fallback - generate from base note if available
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 PLAYBACK
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play a note at base octave
     */
    public int play(Note note) {
        return play(note, baseOctave, 1.0f);
    }
    
    /**
     * Play a note at specific octave
     */
    public int play(Note note, int octave) {
        return play(note, octave, 1.0f);
    }
    
    /**
     * Play a note with custom velocity (volume)
     */
    public int play(Note note, int octave, float velocity) {
        if (soundPool == null || isMuted) return 0;
        
        Integer soundId = noteSoundIds.get(note);
        if (soundId == null) return 0;
        
        // Calculate pitch based on octave difference
        int octaveDiff = octave - baseOctave;
        float pitch = (float) Math.pow(2.0, octaveDiff);
        
        // Clamp pitch to valid range (0.5 - 2.0)
        pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        
        float finalVolume = volume * velocity;
        
        int streamId = soundPool.play(soundId, finalVolume, finalVolume, 1, 0, pitch);
        
        if (listener != null) {
            listener.onNoteStarted(note, octave);
        }
        
        return streamId;
    }
    
    /**
     * Play note by index (0-6)
     */
    public int playByIndex(int index) {
        return play(Note.fromIndex(index));
    }
    
    /**
     * Play note by index with octave
     */
    public int playByIndex(int index, int octave) {
        return play(Note.fromIndex(index), octave, 1.0f);
    }
    
    /**
     * Play note by lane index (mapping lanes to notes)
     */
    public int playForLane(int laneIndex, int totalLanes) {
        // Map lane to note based on total lanes
        int noteIndex;
        if (totalLanes <= 3) {
            // 3 lanes: Do, Mi, Sol
            int[] mapping = {0, 2, 4};
            noteIndex = mapping[laneIndex % 3];
        } else if (totalLanes <= 5) {
            // 5 lanes: Do, Re, Mi, Sol, La
            int[] mapping = {0, 1, 2, 4, 5};
            noteIndex = mapping[laneIndex % 5];
        } else {
            // 7 lanes: All notes
            noteIndex = laneIndex % 7;
        }
        
        return playByIndex(noteIndex);
    }
    
    /**
     * Play a chord (multiple notes)
     */
    public int[] playChord(Note[] notes, int octave) {
        int[] streamIds = new int[notes.length];
        for (int i = 0; i < notes.length; i++) {
            streamIds[i] = play(notes[i], octave, 0.7f);  // Slightly lower volume for chords
        }
        return streamIds;
    }
    
    /**
     * Play major chord
     */
    public int[] playMajorChord(Note root, int octave) {
        // Major chord: root, major 3rd, perfect 5th
        Note third = Note.fromIndex(root.getIndex() + 2);
        Note fifth = Note.fromIndex(root.getIndex() + 4);
        return playChord(new Note[]{root, third, fifth}, octave);
    }
    
    /**
     * Play minor chord
     */
    public int[] playMinorChord(Note root, int octave) {
        // Minor chord: root, minor 3rd, perfect 5th
        Note third = Note.fromIndex(root.getIndex() + 1);  // Simplified
        Note fifth = Note.fromIndex(root.getIndex() + 4);
        return playChord(new Note[]{root, third, fifth}, octave);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎼 SEQUENCES
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play ascending scale
     */
    public void playScale(int startOctave, long intervalMs, Runnable onComplete) {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        for (int i = 0; i < 7; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                play(Note.fromIndex(index), startOctave);
            }, i * intervalMs);
        }
        
        if (onComplete != null) {
            handler.postDelayed(onComplete, 7 * intervalMs);
        }
    }
    
    /**
     * Play success jingle
     */
    public void playSuccessJingle() {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // Do - Mi - Sol - high Do
        handler.postDelayed(() -> play(Note.DO), 0);
        handler.postDelayed(() -> play(Note.MI), 150);
        handler.postDelayed(() -> play(Note.SOL), 300);
        handler.postDelayed(() -> play(Note.DO, baseOctave + 1), 450);
    }
    
    /**
     * Play fail jingle
     */
    public void playFailJingle() {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // Descending: Mi - Re - Do
        handler.postDelayed(() -> play(Note.MI), 0);
        handler.postDelayed(() -> play(Note.RE), 200);
        handler.postDelayed(() -> play(Note.DO), 400);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎚️ VOLUME CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ SETTINGS
    // ═══════════════════════════════════════════════════════════════
    
    public void setBaseOctave(int octave) {
        this.baseOctave = Math.max(2, Math.min(6, octave));
    }
    
    public int getBaseOctave() {
        return baseOctave;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void stop(int streamId) {
        if (soundPool != null && streamId != 0) {
            soundPool.stop(streamId);
        }
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        noteSoundIds.clear();
        isLoaded = false;
    }
}
