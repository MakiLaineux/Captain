package com.technoprimates.captain.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeRepository {

    /** Livedata list of all the codes in the database   */
    private final LiveData<List<Code>> allCodes;

    /** Dao instance */
    private final CodeDao codeDao;

    // Constructor
    public CodeRepository(Application application) {
        CodeRoomDatabase db = CodeRoomDatabase.getDatabase(application);
        codeDao = db.codeDao();
        allCodes = codeDao.getAllCodes();
    }

    /**
     * Insert a <code>Code</code> in the database
     * @param code  The <code>Code</code> to insert
     */
    public void insertCode(@NonNull Code code) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> codeDao.insertCode(code));
        executor.shutdown();
    }

    /**
     * Deletes all <code>Code</code> records matching the given db id
     * @param codeId  database Id of the Code to delete
     */
    public void deleteCode(int codeId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> codeDao.deleteCode(codeId));
        executor.shutdown();
    }

    /**
     * Update a <code>Code</code> in database
     * @param code : the Code to update.
     */
    public void updateCode(@NonNull Code code) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> codeDao.updateCode(code));
        executor.shutdown();
    }

    /**
     * Get a Livedata List of all codes in database
     * @return A {@code Livedata<List<Code>>} object of all codes in database
     */
    public LiveData<List<Code>> getAllCodes() {
        return allCodes;
    }
}
