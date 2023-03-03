package com.technoprimates.captain.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StuckRepository {

    /** Livedata list of all the codes in the database   */
    private final LiveData<List<Stuck>> allStucks;

    /** Dao instance */
    private final StuckDao stuckDao;

    // Constructor
    public StuckRepository(Application application) {
        StuckRoomDatabase db = StuckRoomDatabase.getDatabase(application);
        stuckDao = db.stuckDao();
        allStucks = stuckDao.getAllStucks();
    }

    /**
     * Insert a <code>Stuck</code> in the database
     * @param stuck  The <code>Stuck</code> to insert
     */
    public void insertStuck(@NonNull Stuck stuck) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stuckDao.insertStuck(stuck));
        executor.shutdown();
    }

    /**
     * Deletes all <code>Stuck</code> records matching the given db id
     * @param stuckId  database Id of the Stuck to delete
     */
    public void deleteStuck(int stuckId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stuckDao.deleteStuck(stuckId));
        executor.shutdown();
    }

    /**
     * Update a <code>Stuck</code> in database
     * @param stuck : the Stuck to update.
     */
    public void updateStuck(@NonNull Stuck stuck) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> stuckDao.updateStuck(stuck));
        executor.shutdown();
    }

    /**
     * Get a Livedata List of all items in database
     * @return A {@code Livedata<List<Stuck>>} object of all items in database
     */
    public LiveData<List<Stuck>> getAllStucks() {
        return allStucks;
    }
}
