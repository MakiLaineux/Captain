package com.technoprimates.captain;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.technoprimates.captain.db.Stuck;
import com.technoprimates.captain.db.StuckRepository;

import java.util.List;

/**
 * An application-scoped ViewModel managing Stuck objects
 */
public class StuckViewModel extends AndroidViewModel {

    public static final String TAG = "STUCKVIEWMODEL";

    // Values returned by business logic checkings
    public static final int STUCK_OK = 101;
    public static final int NO_STUCK = 102;
    public static final int NO_STUCK_NAME = 103;
    public static final int STUCK_NAME_ALREADY_EXISTS = 104;

    // Stuck repository
    private final StuckRepository repository;

    // A LiveData list of Codes, to be observed to update the RecylerView
    private final LiveData<List<Stuck>> allstucks;

    // The nature of the next action to be processed
    private int actionMode;

    // A reference to the currently processed Stuck. This is set via the setNextCodeAction method
    private Stuck currentStuck;

    /**
     * Constructor of the ViewModel managing <code>Stuck</code> objects.
     * <p>Do NOT call this directly, use ViewModelProvider instead. </p>
     * @param application   The current application
     */
    public StuckViewModel(Application application) {
        super(application);
        repository = new StuckRepository(application);
        allstucks = repository.getAllStucks();
        currentStuck = null;
    }

    /**
     * Get a LiveData list of all <code>Stuck</code> objects in database
     * <p>The return value is to be observed to update the RecyclerView </p>
     * @return  The LiveData list of Stucks to display
     */
    public LiveData<List<Stuck>> getAllstucks() {return allstucks;}

    /**
     * Deletes the currently selected code
     */
    public void deleteStuck() {
        repository.deleteStuck(currentStuck.getId());
    }

    /**
     * Set the next operation to perform
     * <p>Possible values for the parameters are :</p>
     * <li>Stuck.MODE_UPDATE, with new content of the Stuck to update</li>
     * <li>Stuck.MODE_INSERT, with  <code>null</code></li>
     * <li>Stuck.MODE_DELETE, with Stuck to delete</li>
     *
     * @param operation    The action to be performed
     */
    public void selectActionToProcess(int operation)
    {
        actionMode = operation;
    }

    /**
     * Set the <code>Stuck</code> object to process in the next operation.
     * @param stuck      A <code>Stuck</code> object already existing in the database.
     */
    public void selectStuckToProcess(Stuck stuck)
    {
        currentStuck = stuck;
    }

    /**
     * Set the UI Fields of the <code>Stuck</code> object to process in the next operation.
     * @param stuck      A <code>Stuck</code> object containing the user-provided fields.
     */
    public void fillStuckToProcess(@NonNull Stuck stuck)
    {
        if (currentStuck == null) {
            Log.e(TAG, "No currentStuck set");
            return;
        }
        currentStuck.setUserProvidedFields(stuck);
    }

    /**
     * Get db-existing Stuck to process next.
     * <p>The Stuck to process must have been set previously with the <code>setNextCodeAction</code> method</p>
     * @return  A <code>Stuck</code> object already existing in the database, or null if no Stuck object was previously set.
     */
    public Stuck getStuckToProcess() {
        return currentStuck;
    }


    /**
     * Get the next action to be performed.
     * <p>Possible values are :</p>
     * <li>Stuck.MODE_VISU</li>
     * <li>Stuck.MODE_UPDATE</li>
     * <li>Stuck.MODE_INSERT</li>
     * <li>Stuck.MODE_DELETE</li>
     *
     * @return  The action to be performed
     */
    public int getActionMode() {
        return actionMode;
    }

    /**
     * Update a Stuck.
     */
    public void updateStuck() {
        // before updating, set the UpdateDay value in format dd-MM-yyyy
        currentStuck.setUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.updateStuck(currentStuck);
    }

    /**
     * Insert a new <code>Stuck</code>.
     * <p>The <code>Stuck</code> to insert must have been set </p>
     */
    public void insertStuck() {
        /*
        The Stuck to insert must have been set in the currentItem member
        The codeId value is zero which leads to autoincrementation in the room database.
        The updateDay value is set to the day of the current system Date
         */
        // set the UpdateDay value in format dd-MM-yyyy
        currentStuck.setUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.insertStuck(currentStuck);
    }

    /**
     * Reinsert the <code>Stuck</code> that was just deleted.
     * Call this method if the user cancels the deletion of a Stuck. The <code>Stuck</code> will be inserted with all his previous fields,
     * and it will keep its previous previous dbId and updateDay values
     */
    public void reInsertStuck() {
        repository.insertStuck(currentStuck);
    }


    public int checkStuckBusinessLogic(Stuck stuck, int actionMode) {
        // The stuck must not be null
        if (stuck == null)
            return NO_STUCK;

        // Refuse insertion with empty or already existing stuck name
        if (actionMode == Stuck.MODE_INSERT) {
            if (stuck.getName().equals(""))
                return NO_STUCK_NAME;
            if (stuckNameAlreadyExists(stuck)) {
                return STUCK_NAME_ALREADY_EXISTS;
            }
        }

        // Refuse update if the stuck name provided is changed
        // to an already existing stuck name
        if (actionMode == Stuck.MODE_UPDATE) {
            if ((!stuck.getName().equals(currentStuck.getName()))
                && (stuckNameAlreadyExists(stuck)))
                return STUCK_NAME_ALREADY_EXISTS;
            }

        // All checks completed
        return STUCK_OK;
    }

    // check that a Stuck matching the name of the given Stuck does not already exists in current stuck list
    // returns true if the name is NOT in the list
    private boolean stuckNameAlreadyExists(@NonNull Stuck stuck) {

        if ((allstucks == null) || (allstucks.getValue() == null))
            return false;

        for (Stuck c : allstucks.getValue()) {
            if (c.getName().equals(stuck.getName())) {
                return true;
            }
        }
        return false; // no match found
    }
}