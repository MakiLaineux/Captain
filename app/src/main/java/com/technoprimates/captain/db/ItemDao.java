package com.technoprimates.captain.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {

    /**
     * Insert a <code>Item</code> in the database
     * @param item  The <code>Item</code> to be inserted
     */
    @Insert
    void insertItem(Item item);

    /**
     * Search for a <code>Item</code> with his name.
     * @param name  The name to be searched
     * @return      A {@code List<Item>} object which may content multiple elements
     */
    @Query("SELECT * FROM items WHERE cName = :name")
    List<Item> findItem(String name);

    /**
     * Gets all the <code>Item</code> records in database
     * @return  A livedata list of <code>Item</code>
     */
    @Query("SELECT * FROM items")
    LiveData<List<Item>> getAllItems();

    /**
     * Delete the <code>Item</code> database records matching the given database id
     * @param codeId database Id of the code to be deleted
     */
    @Query("DELETE FROM items WHERE cId = :codeId")
    void deleteItem(int codeId);

    /**
     * Update the record in database
     * @param item  The <code>Item</code> object to be updated
     */
    @Update
    void updateItem(Item item);
}
