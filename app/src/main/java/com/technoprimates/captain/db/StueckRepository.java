package com.technoprimates.captain.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StueckRepository {

    /** Livedata list of all the stuecks in the database   */
    private final LiveData<List<Stueck>> allStuecksList;

    /** Dao instance */
    private final StueckDao stueckDao;

    // Constructor
    public StueckRepository(Application application) {
        StueckRoomDatabase db = StueckRoomDatabase.getDatabase(application);
        stueckDao = db.stueckDao();
        allStuecksList = stueckDao.getAllStuecks();
    }

    /**
     * Insert a <code>Stueck</code> in the database
     * @param stueck  The <code>Stueck</code> to insert
     */
    public void insertStueck(@NonNull Stueck stueck) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stueckDao.insertStueck(stueck));
        executor.shutdown();
    }

    /**
     * Deletes all <code>Stueck</code> records matching the given db id
     * @param stueckId  database Id of the Stueck to delete
     */
    public void deleteStueck(int stueckId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stueckDao.deleteStueck(stueckId));
        executor.shutdown();
    }

    /**
     * Update a <code>Stueck</code> in database
     * @param stueck : the Stueck to update.
     */
    public void updateStueck(@NonNull Stueck stueck) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stueckDao.updateStueck(stueck));
        executor.shutdown();
    }

    /**
     * Get a Livedata List of all items in database
     * @return A {@code Livedata<List<Stueck>>} object of all items in database
     */
    public LiveData<List<Stueck>> getAllStuecksList() {
        return allStuecksList;
    }
}
