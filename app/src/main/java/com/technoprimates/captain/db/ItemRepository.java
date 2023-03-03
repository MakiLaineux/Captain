package com.technoprimates.captain.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemRepository {

    /** Livedata list of all the codes in the database   */
    private final LiveData<List<Item>> allItems;

    /** Dao instance */
    private final ItemDao itemDao;

    // Constructor
    public ItemRepository(Application application) {
        ItemRoomDatabase db = ItemRoomDatabase.getDatabase(application);
        itemDao = db.itemDao();
        allItems = itemDao.getAllItems();
    }

    /**
     * Insert a <item>Item</item> in the database
     * @param item  The <item>Item</item> to insert
     */
    public void insertItem(@NonNull Item item) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> itemDao.insertItem(item));
        executor.shutdown();
    }

    /**
     * Deletes all <code>Item</code> records matching the given db id
     * @param itemId  database Id of the Item to delete
     */
    public void deleteItem(int itemId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> itemDao.deleteItem(itemId));
        executor.shutdown();
    }

    /**
     * Update a <item>Item</item> in database
     * @param item : the Item to update.
     */
    public void updateItem(@NonNull Item item) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> itemDao.updateItem(item));
        executor.shutdown();
    }

    /**
     * Get a Livedata List of all items in database
     * @return A {@code Livedata<List<Item>>} object of all items in database
     */
    public LiveData<List<Item>> getAllItems() {
        return allItems;
    }
}
