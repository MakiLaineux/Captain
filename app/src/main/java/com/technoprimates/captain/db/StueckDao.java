package com.technoprimates.captain.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StueckDao {

    /**
     * Insert a <code>Stueck</code> in the database
     * @param stueck  The <code>Stueck</code> to be inserted
     */
    @Insert
    void insertStueck(Stueck stueck);

    /**
     * Search for a <code>Stueck</code> with his name.
     * @param name  The name to be searched
     * @return      A {@code List<Stueck>} object which may content multiple elements
     */
    @Query("SELECT * FROM stuecks WHERE sName = :name")
    List<Stueck> findStueck(String name);

    /**
     * Gets all the <code>Stueck</code> records in database
     * @return  A livedata list of <code>Stueck</code>
     */
    @Query("SELECT * FROM stuecks")
    LiveData<List<Stueck>> getAllStuecks();

    /**
     * Delete the <code>Stueck</code> database records matching the given database id
     * @param id database Id of the code to be deleted
     */
    @Query("DELETE FROM stuecks WHERE sId = :id")
    void deleteStueck(int id);

    /**
     * Update the record in database
     * @param stueck  The <code>Stueck</code> object to be updated
     */
    @Update
    void updateStueck(Stueck stueck);
}
