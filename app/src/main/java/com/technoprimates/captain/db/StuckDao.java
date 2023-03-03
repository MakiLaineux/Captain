package com.technoprimates.captain.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StuckDao {

    /**
     * Insert a <code>Stuck</code> in the database
     * @param stuck  The <code>Stuck</code> to be inserted
     */
    @Insert
    void insertStuck(Stuck stuck);

    /**
     * Search for a <code>Stuck</code> with his name.
     * @param name  The name to be searched
     * @return      A {@code List<Stuck>} object which may content multiple elements
     */
    @Query("SELECT * FROM stucks WHERE sName = :name")
    List<Stuck> findStuck(String name);

    /**
     * Gets all the <code>Stuck</code> records in database
     * @return  A livedata list of <code>Stuck</code>
     */
    @Query("SELECT * FROM stucks")
    LiveData<List<Stuck>> getAllStucks();

    /**
     * Delete the <code>Stuck</code> database records matching the given database id
     * @param id database Id of the code to be deleted
     */
    @Query("DELETE FROM stucks WHERE sId = :id")
    void deleteStuck(int id);

    /**
     * Update the record in database
     * @param stuck  The <code>Stuck</code> object to be updated
     */
    @Update
    void updateStuck(Stuck stuck);
}
