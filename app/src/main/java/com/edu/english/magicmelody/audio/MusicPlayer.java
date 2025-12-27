package com.edu.english.magicmelody.audio;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎵 Music Player
 * 
 * ExoPlayer wrapper for background music:
 * - BGM playback
 * - Crossfade between tracks
 * - Loop and shuffle support
 */
public class MusicPlayer {
    
    private static final String TAG = "MusicPlayer";
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    public enum PlayerState {
        IDLE,
        LOADING,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR
    }
    
    private Context context;
    private ExoPlayer player;
    private ExoPlayer crossfadePlayer;  // For crossfade transitions
    
    private PlayerState currentState = PlayerState.IDLE;
    private String currentTrackId;
    private Uri currentUri;
    
    // Playlist
    private List<MusicTrack> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = true;
    
    // Volume
    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;  // Default lower for BGM
    private boolean isMuted = false;
    
    // Crossfade
    private boolean crossfadeEnabled = true;
    private long crossfadeDurationMs = 2000;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // ═══════════════════════════════════════════════════════════════
    // 📦 MUSIC TRACK
    // ═══════════════════════════════════════════════════════════════
    
    public static class MusicTrack {
        public String id;
        public String title;
        public String artist;
        public Uri uri;
        public String worldId;      // Associated world
        public String mood;         // "calm", "energetic", "boss"
        public float bpm;
        
        public MusicTrack(String id, String title, Uri uri) {
            this.id = id;
            this.title = title;
            this.uri = uri;
        }
        
        public MusicTrack withBpm(float bpm) {
            this.bpm = bpm;
            return this;
        }
        
        public MusicTrack withMood(String mood) {
            this.mood = mood;
            return this;
        }
        
        public MusicTrack forWorld(String worldId) {
            this.worldId = worldId;
            return this;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📡 LISTENER
    // ═══════════════════════════════════════════════════════════════
    
    public interface MusicPlayerListener {
        void onStateChanged(PlayerState state);
        void onTrackChanged(MusicTrack track);
        void onPositionChanged(long positionMs, long durationMs);
        void onError(String error);
    }
    
    private MusicPlayerListener listener;
    
    // ═══════════════════════════════════════════════════════════════
    // 🏗️ SINGLETON
    // ═══════════════════════════════════════════════════════════════
    
    private static MusicPlayer instance;
    
    public static synchronized MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }
    
    private MusicPlayer() {
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    public void setListener(MusicPlayerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize player
     */
    @OptIn(markerClass = UnstableApi.class)
    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        
        player = new ExoPlayer.Builder(context).build();
        player.addListener(createPlayerListener());
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        
        updateVolume();
    }
    
    private Player.Listener createPlayerListener() {
        return new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_IDLE:
                        setState(PlayerState.IDLE);
                        break;
                    case Player.STATE_BUFFERING:
                        setState(PlayerState.LOADING);
                        break;
                    case Player.STATE_READY:
                        if (player.isPlaying()) {
                            setState(PlayerState.PLAYING);
                        }
                        break;
                    case Player.STATE_ENDED:
                        onTrackEnded();
                        break;
                }
            }
            
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    setState(PlayerState.PLAYING);
                    startPositionUpdates();
                } else if (currentState == PlayerState.PLAYING) {
                    setState(PlayerState.PAUSED);
                    stopPositionUpdates();
                }
            }
            
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Playback error: " + error.getMessage());
                setState(PlayerState.ERROR);
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        };
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 PLAYBACK
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Play a track by URI
     */
    public void play(Uri uri) {
        if (player == null) return;
        
        currentUri = uri;
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }
    
    /**
     * Play a track from assets
     */
    public void playFromAssets(String path) {
        Uri uri = Uri.parse("asset:///magicmelody/music/" + path);
        play(uri);
    }
    
    /**
     * Play a MusicTrack
     */
    public void play(MusicTrack track) {
        if (crossfadeEnabled && isPlaying()) {
            crossfadeTo(track);
        } else {
            currentTrackId = track.id;
            play(track.uri);
            
            if (listener != null) {
                listener.onTrackChanged(track);
            }
        }
    }
    
    /**
     * Crossfade to new track
     */
    @OptIn(markerClass = UnstableApi.class)
    private void crossfadeTo(MusicTrack newTrack) {
        if (player == null) return;
        
        // Create crossfade player
        crossfadePlayer = new ExoPlayer.Builder(context).build();
        crossfadePlayer.setVolume(0f);
        crossfadePlayer.setMediaItem(MediaItem.fromUri(newTrack.uri));
        crossfadePlayer.prepare();
        crossfadePlayer.play();
        
        // Animate crossfade
        final float startVolume = getEffectiveVolume();
        final long startTime = System.currentTimeMillis();
        
        Runnable crossfadeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, (float) elapsed / crossfadeDurationMs);
                
                // Fade out old
                player.setVolume(startVolume * (1 - progress));
                // Fade in new
                crossfadePlayer.setVolume(startVolume * progress);
                
                if (progress < 1f) {
                    handler.postDelayed(this, 50);
                } else {
                    // Swap players
                    ExoPlayer oldPlayer = player;
                    player = crossfadePlayer;
                    crossfadePlayer = null;
                    
                    oldPlayer.stop();
                    oldPlayer.release();
                    
                    currentTrackId = newTrack.id;
                    if (listener != null) {
                        listener.onTrackChanged(newTrack);
                    }
                }
            }
        };
        
        handler.post(crossfadeRunnable);
    }
    
    /**
     * Pause playback
     */
    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }
    
    /**
     * Resume playback
     */
    public void resume() {
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        if (player != null) {
            player.stop();
            setState(PlayerState.STOPPED);
        }
    }
    
    /**
     * Seek to position
     */
    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
        }
    }
    
    /**
     * Handle track end
     */
    private void onTrackEnded() {
        if (repeatEnabled && !playlist.isEmpty()) {
            // Play next or repeat
            if (currentIndex < playlist.size() - 1) {
                playNext();
            } else {
                currentIndex = 0;
                play(playlist.get(0));
            }
        } else {
            setState(PlayerState.STOPPED);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📋 PLAYLIST
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Set playlist
     */
    public void setPlaylist(List<MusicTrack> tracks) {
        this.playlist = new ArrayList<>(tracks);
        this.currentIndex = 0;
    }
    
    /**
     * Add track to playlist
     */
    public void addToPlaylist(MusicTrack track) {
        playlist.add(track);
    }
    
    /**
     * Play next track
     */
    public void playNext() {
        if (playlist.isEmpty()) return;
        
        if (shuffleEnabled) {
            currentIndex = (int)(Math.random() * playlist.size());
        } else {
            currentIndex = (currentIndex + 1) % playlist.size();
        }
        
        play(playlist.get(currentIndex));
    }
    
    /**
     * Play previous track
     */
    public void playPrevious() {
        if (playlist.isEmpty()) return;
        
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        play(playlist.get(currentIndex));
    }
    
    /**
     * Play track at index
     */
    public void playAt(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentIndex = index;
            play(playlist.get(index));
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🎚️ VOLUME CONTROL
    // ═══════════════════════════════════════════════════════════════
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        updateVolume();
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        updateVolume();
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        updateVolume();
    }
    
    private void updateVolume() {
        if (player != null) {
            player.setVolume(getEffectiveVolume());
        }
    }
    
    private float getEffectiveVolume() {
        if (isMuted) return 0f;
        return masterVolume * musicVolume;
    }
    
    /**
     * Fade volume
     */
    public void fadeVolume(float targetVolume, long durationMs) {
        if (player == null) return;
        
        final float startVolume = player.getVolume();
        final float volumeDiff = targetVolume - startVolume;
        final long startTime = System.currentTimeMillis();
        
        Runnable fadeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, (float) elapsed / durationMs);
                
                float newVolume = startVolume + (volumeDiff * progress);
                player.setVolume(newVolume);
                
                if (progress < 1f) {
                    handler.postDelayed(this, 50);
                }
            }
        };
        
        handler.post(fadeRunnable);
    }
    
    /**
     * Fade out and pause
     */
    public void fadeOutAndPause(long durationMs) {
        fadeVolume(0f, durationMs);
        handler.postDelayed(this::pause, durationMs);
    }
    
    /**
     * Fade in from pause
     */
    public void fadeInAndResume(long durationMs) {
        if (player != null) {
            player.setVolume(0f);
            resume();
            fadeVolume(getEffectiveVolume(), durationMs);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⏱️ POSITION UPDATES
    // ═══════════════════════════════════════════════════════════════
    
    private Runnable positionUpdateRunnable;
    
    private void startPositionUpdates() {
        stopPositionUpdates();
        
        positionUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && listener != null) {
                    listener.onPositionChanged(
                        player.getCurrentPosition(),
                        player.getDuration()
                    );
                }
                handler.postDelayed(this, 500);
            }
        };
        
        handler.post(positionUpdateRunnable);
    }
    
    private void stopPositionUpdates() {
        if (positionUpdateRunnable != null) {
            handler.removeCallbacks(positionUpdateRunnable);
            positionUpdateRunnable = null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ⚙️ SETTINGS
    // ═══════════════════════════════════════════════════════════════
    
    public void setShuffleEnabled(boolean enabled) {
        this.shuffleEnabled = enabled;
    }
    
    public void setRepeatEnabled(boolean enabled) {
        this.repeatEnabled = enabled;
        if (player != null) {
            player.setRepeatMode(enabled ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        }
    }
    
    public void setCrossfadeEnabled(boolean enabled) {
        this.crossfadeEnabled = enabled;
    }
    
    public void setCrossfadeDuration(long durationMs) {
        this.crossfadeDurationMs = durationMs;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 STATE
    // ═══════════════════════════════════════════════════════════════
    
    private void setState(PlayerState state) {
        if (currentState != state) {
            currentState = state;
            if (listener != null) {
                listener.onStateChanged(state);
            }
        }
    }
    
    public PlayerState getCurrentState() {
        return currentState;
    }
    
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }
    
    public boolean isPaused() {
        return currentState == PlayerState.PAUSED;
    }
    
    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }
    
    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }
    
    public String getCurrentTrackId() {
        return currentTrackId;
    }
    
    public MusicTrack getCurrentTrack() {
        if (!playlist.isEmpty() && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔄 CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    public void release() {
        stopPositionUpdates();
        handler.removeCallbacksAndMessages(null);
        
        if (player != null) {
            player.release();
            player = null;
        }
        
        if (crossfadePlayer != null) {
            crossfadePlayer.release();
            crossfadePlayer = null;
        }
        
        listener = null;
        currentState = PlayerState.IDLE;
    }
}
