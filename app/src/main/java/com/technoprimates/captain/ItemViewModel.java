package com.technoprimates.captain;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.technoprimates.captain.db.Item;
import com.technoprimates.captain.db.ItemRepository;

import java.util.List;

/**
 * An application-scoped ViewModel managing Item objects
 */
public class ItemViewModel extends AndroidViewModel {

    public static final String TAG = "CODEVIEWMODEL";

    // Values returned by business logic checkings
    public static final int ITEM_OK = 101;
    public static final int NO_ITEM = 102;
    public static final int NO_ITEMNAME = 103;
    public static final int ITEMNAME_ALREADY_EXISTS = 104;

    // Item repository
    private final ItemRepository repository;

    // A LiveData list of Codes, to be observed to update the RecylerView
    private final LiveData<List<Item>> allitems;

    // The nature of the next action to be processed
    private int actionMode;

    // A reference to the currently processed Item. This is set via the setNextCodeAction method
    private Item currentItem;

    /**
     * Constructor of the ViewModel managing <code>Item</code> objects.
     * <p>Do NOT call this directly, use ViewModelProvider instead. </p>
     * @param application   The current application
     */
    public ItemViewModel(Application application) {
        super(application);
        repository = new ItemRepository(application);
        allitems = repository.getAllItems();
        currentItem = null;
    }

    /**
     * Get a LiveData list of all <code>Item</code> objects in database
     * <p>The return value is to be observed to update the RecyclerView </p>
     * @return  The LiveData list of Codes to display
     */
    public LiveData<List<Item>> getAllitems() {return allitems;}

    /**
     * Deletes the currently selected code
     */
    public void deleteItem() {
        int itemId = currentItem.getItemId();
        repository.deleteItem(itemId);
    }

    /**
     * Set the next operation to perform
     * <p>Possible values for the parameters are :</p>
     * <li>Item.MODE_VISU, with Item to display </li>
     * <li>Item.MODE_UPDATE, with new content of the Item to update</li>
     * <li>Item.MODE_INSERT, with  <code>null</code></li>
     * <li>Item.MODE_DELETE, with Item to delete</li>
     *
     * @param operation    The action to be performed
     */
    public void selectActionToProcess(int operation)
    {
        actionMode = operation;
    }

    /**
     * Set the <item>Item</item> object to process in the next operation.
     * @param item      A <item>Item</item> object already existing in the database.
     */
    public void selectItemToProcess(Item item)
    {
        currentItem = item;
    }

    /**
     * Set the UI Fields of the <item>Item</item> object to process in the next operation.
     * @param item      A <item>Item</item> object containing the user-provided fields.
     */
    public void fillItemToProcess(@NonNull Item item)
    {
        if (currentItem == null) {
            Log.e(TAG, "No currentItem set");
            return;
        }
        currentItem.setUserProvidedFields(item);
    }

    /**
     * Get db-existing Item to process next.
     * <p>The Item to process must have been set previously with the <code>setNextCodeAction</code> method</p>
     * @return  A <code>Item</code> object already existing in the database, or null if no Item object was previously set.
     */
    public Item getItemToProcess() {
        return currentItem;
    }


    /**
     * Get the next action to be performed.
     * <p>Possible values are :</p>
     * <li>Item.MODE_VISU</li>
     * <li>Item.MODE_UPDATE</li>
     * <li>Item.MODE_INSERT</li>
     * <li>Item.MODE_DELETE</li>
     *
     * @return  The action to be performed
     */
    public int getActionMode() {
        return actionMode;
    }

    /**
     * Update a Item.
     */
    public void updateItem() {
        // before updating, set the UpdateDay value in format dd-MM-yyyy
        currentItem.setItemUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.updateItem(currentItem);
    }

    /**
     * Insert a new <code>Item</code>.
     * <p>The <code>Item</code> to insert must have been set </p>
     */
    public void insertItem() {
        /*
        The Item to insert must have been set in the currentItem member
        The codeId value is zero which leads to autoincrementation in the room database.
        The updateDay value is set to the day of the current system Date
         */
        // set the UpdateDay value in format dd-MM-yyyy
        currentItem.setItemUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.insertItem(currentItem);
    }

    /**
     * Reinsert the <code>Item</code> that was just deleted.
     * Call this method if the user cancels the deletion of a Item. The <code>Item</code> will be inserted with all his previous fields,
     * and it will keep its previous previous dbId and updateDay values
     */
    public void reInsertItem() {
        repository.insertItem(currentItem);
    }


    public int checkItemBusinessLogic(Item item, int actionMode) {
        // The item must not be null
        if (item == null)
            return NO_ITEM;

        // Refuse insertion with empty or already existing item name
        if (actionMode == Item.MODE_INSERT) {
            if (item.getItemName().equals(""))
                return NO_ITEMNAME;
            if (itemnameAlreadyExists(item)) {
                return ITEMNAME_ALREADY_EXISTS;
            }
        }

        // Refuse update if the item name provided is changed
        // to an already existing item name
        if (actionMode == Item.MODE_UPDATE) {
            if ((!item.getItemName().equals(currentItem.getItemName()))
                && (itemnameAlreadyExists(item)))
                return ITEMNAME_ALREADY_EXISTS;
            }

        // All checks completed
        return ITEM_OK;
    }

    // check that a Item matching the name of the given Item does not already exists in current item list
    // returns true if the name is NOT in the list
    private boolean itemnameAlreadyExists(@NonNull Item item) {

        if ((allitems == null) || (allitems.getValue() == null))
            return false;

        for (Item c : allitems.getValue()) {
            if (c.getItemName().equals(item.getItemName())) {
                return true;
            }
        }
        return false; // no match found
    }
}