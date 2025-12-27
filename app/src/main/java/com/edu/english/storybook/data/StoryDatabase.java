package com.edu.english.storybook.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for Magic Storybook
 */
@Database(entities = {StoryEntity.class}, version = 1, exportSchema = false)
public abstract class StoryDatabase extends RoomDatabase {
    
    private static volatile StoryDatabase INSTANCE;
    private static final String DATABASE_NAME = "magic_storybook_db";
    
    public abstract StoryDao storyDao();
    
    public static StoryDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (StoryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            StoryDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
