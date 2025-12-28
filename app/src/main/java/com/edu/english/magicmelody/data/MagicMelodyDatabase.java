package com.edu.english.magicmelody.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.edu.english.magicmelody.data.dao.BossProgressDao;
import com.edu.english.magicmelody.data.dao.CollectedNoteDao;
import com.edu.english.magicmelody.data.dao.LessonProgressDao;
import com.edu.english.magicmelody.data.dao.UserProfileDao;
import com.edu.english.magicmelody.data.dao.WorldProgressDao;
import com.edu.english.magicmelody.data.entity.BossProgress;
import com.edu.english.magicmelody.data.entity.CollectedNote;
import com.edu.english.magicmelody.data.entity.LessonProgress;
import com.edu.english.magicmelody.data.entity.UserProfile;
import com.edu.english.magicmelody.data.entity.WorldProgress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🎵 Magic Melody Room Database
 * 
 * Purpose: Central database for all Magic Melody game data
 * 
 * Entities:
 * - UserProfile: User settings and progress summary
 * - LessonProgress: Individual lesson completion data
 * - WorldProgress: World unlock and evolution tracking
 * - CollectedNote: Magic Notebook notes collection
 * - BossProgress: Boss battle history and achievements
 */
@Database(
    entities = {
        UserProfile.class,
        LessonProgress.class,
        WorldProgress.class,
        CollectedNote.class,
        BossProgress.class
    },
    version = 2,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class MagicMelodyDatabase extends RoomDatabase {
    
    // ═══════════════════════════════════════════════════════════════
    // 📌 SINGLETON INSTANCE
    // ═══════════════════════════════════════════════════════════════
    
    private static volatile MagicMelodyDatabase INSTANCE;
    private static final String DATABASE_NAME = "magic_melody_database";
    
    // Number of threads for database operations
    private static final int NUMBER_OF_THREADS = 4;
    
    /**
     * Executor service for running database operations on background threads
     */
    public static final ExecutorService databaseWriteExecutor = 
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    // ═══════════════════════════════════════════════════════════════
    // 📊 DAO ACCESSORS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get UserProfile DAO
     */
    public abstract UserProfileDao userProfileDao();
    
    /**
     * Get LessonProgress DAO
     */
    public abstract LessonProgressDao lessonProgressDao();
    
    /**
     * Get WorldProgress DAO
     */
    public abstract WorldProgressDao worldProgressDao();
    
    /**
     * Get CollectedNote DAO
     */
    public abstract CollectedNoteDao collectedNoteDao();
    
    /**
     * Get BossProgress DAO
     */
    public abstract BossProgressDao bossProgressDao();
    
    // ═══════════════════════════════════════════════════════════════
    // 🔧 SINGLETON ACCESS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Get the singleton database instance
     * 
     * @param context Application context
     * @return MagicMelodyDatabase instance
     */
    public static MagicMelodyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MagicMelodyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MagicMelodyDatabase.class,
                            DATABASE_NAME
                    )
                    // Allow destructive migration for development
                    // Remove in production and use proper migration
                    .fallbackToDestructiveMigration()
                    
                    // Enable multi-instance invalidation for better performance
                    // .enableMultiInstanceInvalidation()
                    
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Close the database instance
     * Call this when the application is being destroyed
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Run a database operation in background
     * 
     * @param operation Runnable containing database operations
     */
    public static void runAsync(Runnable operation) {
        databaseWriteExecutor.execute(operation);
    }
    
    /**
     * Clear all data (for testing or reset)
     */
    public void clearAllData() {
        databaseWriteExecutor.execute(() -> {
            clearAllTables();
        });
    }
}
