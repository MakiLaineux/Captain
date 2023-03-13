package com.technoprimates.captain.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Stueck.class}, version = 1)
public abstract class StueckRoomDatabase extends RoomDatabase {
    public abstract StueckDao stueckDao();
    private static volatile StueckRoomDatabase INSTANCE;

    static StueckRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StueckRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    StueckRoomDatabase.class, "stueck-database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
