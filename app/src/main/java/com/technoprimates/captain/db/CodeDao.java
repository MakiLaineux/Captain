package com.technoprimates.captain.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CodeDao {

    /**
     * Insert a <code>Code</code> in the database
     * @param code  The <code>Code</code> to be inserted
     */
    @Insert
    void insertCode(Code code);

    /**
     * Search for a <code>Code</code> with his name.
     * @param name  The name to be searched
     * @return      A {@code List<Code>} object which may content multiple elements
     */
    @Query("SELECT * FROM codes WHERE cName = :name")
    List<Code> findCode(String name);

    /**
     * Gets all the <code>Code</code> records in database
     * @return  A livedata list of <code>Code</code>
     */
    @Query("SELECT * FROM codes")
    LiveData<List<Code>> getAllCodes();

    /**
     * Delete the <code>Code</code> database records matching the given database id
     * @param codeId database Id of the code to be deleted
     */
    @Query("DELETE FROM codes WHERE cId = :codeId")
    void deleteCode(int codeId);

    /**
     * Update the record in database
     * @param code  The <code>Code</code> object to be updated
     */
    @Update
    void updateCode(Code code);
}
