package com.technoprimates.captain.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Code.class}, version = 2)
public abstract class CodeRoomDatabase extends RoomDatabase {
    public abstract CodeDao codeDao();
    private static volatile CodeRoomDatabase INSTANCE;

    // Database version 2 adds the "comments" field
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE codes "
                    + " ADD COLUMN cComments TEXT");
        }
    };

    static CodeRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CodeRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CodeRoomDatabase.class, "code-database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
