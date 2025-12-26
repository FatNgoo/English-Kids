package com.edu.english.magicmelody.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.edu.english.magicmelody.data.model.CollectionEntity;
import com.edu.english.magicmelody.data.model.MistakeEntity;
import com.edu.english.magicmelody.data.model.ProgressEntity;

/**
 * Room Database for Magic Melody game
 * Stores collections, progress, and mistakes
 */
@Database(
    entities = {
        CollectionEntity.class,
        ProgressEntity.class,
        MistakeEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class MagicMelodyDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "magic_melody_db";
    private static volatile MagicMelodyDatabase INSTANCE;
    
    // DAOs
    public abstract CollectionDao collectionDao();
    public abstract ProgressDao progressDao();
    public abstract MistakeDao mistakeDao();
    
    /**
     * Get singleton instance of database
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
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Clear singleton instance (for testing)
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
