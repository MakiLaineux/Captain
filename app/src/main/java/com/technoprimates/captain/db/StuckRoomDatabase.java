package com.technoprimates.captain.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Stuck.class}, version = 1)
public abstract class StuckRoomDatabase extends RoomDatabase {
    public abstract StuckDao stuckDao();
    private static volatile StuckRoomDatabase INSTANCE;

    static StuckRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StuckRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    StuckRoomDatabase.class, "stuck-database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
