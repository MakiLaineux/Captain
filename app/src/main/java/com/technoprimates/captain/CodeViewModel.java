package com.technoprimates.captain;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.technoprimates.captain.db.Code;
import com.technoprimates.captain.db.CodeRepository;

import java.util.List;

/**
 * An application-scoped ViewModel managing Code objects
 */
public class CodeViewModel extends AndroidViewModel {

    public static final String TAG = "CODEVIEWMODEL";

    // Values returned by business logic checkings
    public static final int CODE_OK = 101;
    public static final int NO_CODE = 102;
    public static final int NO_CODENAME = 103;
    public static final int CODENAME_ALREADY_EXISTS = 104;

    // Code repository
    private final CodeRepository repository;

    // A LiveData list of Codes, to be observed to update the RecylerView
    private final LiveData<List<Code>> allcodes;

    // The nature of the next action to be processed
    private int actionMode;

    // A reference to the currently processed Code. This is set via the setNextCodeAction method
    private Code currentCode;

    /**
     * Constructor of the ViewModel managing <code>Code</code> objects.
     * <p>Do NOT call this directly, use ViewModelProvider instead. </p>
     * @param application   The current application
     */
    public CodeViewModel(Application application) {
        super(application);
        repository = new CodeRepository(application);
        allcodes = repository.getAllCodes();
        currentCode = null;
    }

    /**
     * Get a LiveData list of all <code>Code</code> objects in database
     * <p>The return value is to be observed to update the RecyclerView </p>
     * @return  The LiveData list of Codes to display
     */
    public LiveData<List<Code>> getAllcodes() {return allcodes;}

    /**
     * Deletes the currently selected code
     */
    public void deleteCode() {
        int codeId = currentCode.getCodeId();
        repository.deleteCode(codeId);
    }

    /**
     * Set the next operation to perform
     * <p>Possible values for the parameters are :</p>
     * <li>Code.MODE_VISU, with Code to display </li>
     * <li>Code.MODE_UPDATE, with new content of the Code to update</li>
     * <li>Code.MODE_INSERT, with  <code>null</code></li>
     * <li>Code.MODE_DELETE, with Code to delete</li>
     *
     * @param operation    The action to be performed
     */
    public void selectActionToProcess(int operation)
    {
        actionMode = operation;
    }

    /**
     * Set the <code>Code</code> object to process in the next operation.
     * @param code      A <code>Code</code> object already existing in the database.
     */
    public void selectCodeToProcess(Code code )
    {
        currentCode = code;
    }

    /**
     * Set the UI Fields of the <code>Code</code> object to process in the next operation.
     * @param code      A <code>Code</code> object containing the user-provided fields.
     */
    public void fillCodeToProcess(@NonNull Code code )
    {
        if (currentCode == null) {
            Log.e(TAG, "No currentCode set");
            return;
        }
        currentCode.setUserProvidedFields(code);
    }

    /**
     * Get db-existing Code to process next.
     * <p>The Code to process must have been set previously with the <code>setNextCodeAction</code> method</p>
     * @return  A <code>Code</code> object already existing in the database, or null if no Code object was previously set.
     */
    public Code getCodeToProcess() {
        return currentCode;
    }


    /**
     * Get the next action to be performed.
     * <p>Possible values are :</p>
     * <li>Code.MODE_VISU</li>
     * <li>Code.MODE_UPDATE</li>
     * <li>Code.MODE_INSERT</li>
     * <li>Code.MODE_DELETE</li>
     *
     * @return  The action to be performed
     */
    public int getActionMode() {
        return actionMode;
    }

    /**
     * Update a Code.
     */
    public void updateCode() {
        // before updating, set the UpdateDay value in format dd-MM-yyyy
        currentCode.setCodeUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.updateCode(currentCode);
    }

    /**
     * Insert a new <code>Code</code>.
     * <p>The <code>Code</code> to insert must have been set </p>
     */
    public void insertCode() {
        /*
        The Code to insert must have been set in the currentCode member
        The codeId value is zero which leads to autoincrementation in the room database.
        The updateDay value is set to the day of the current system Date
         */
        // set the UpdateDay value in format dd-MM-yyyy
        currentCode.setCodeUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.insertCode(currentCode);
    }

    /**
     * Reinsert the <code>Code</code> that was just deleted.
     * Call this method if the user cancels the deletion of a Code. The <code>Code</code> will be inserted with all his previous fields,
     * and it will keep its previous previous dbId and updateDay values
     */
    public void reInsertCode() {
        repository.insertCode(currentCode);
    }


    public int checkCodeBusinessLogic(Code code, int actionMode) {
        // The code must not be null
        if (code == null)
            return NO_CODE;

        // Refuse insertion with empty or already existing code name
        if (actionMode == Code.MODE_INSERT) {
            if (code.getCodeName().equals(""))
                return NO_CODENAME;
            if (codenameAlreadyExists(code)) {
                return CODENAME_ALREADY_EXISTS;
            }
        }

        // Refuse update if the code name provided is changed
        // to an already existing code name
        if (actionMode == Code.MODE_UPDATE) {
            if ((!code.getCodeName().equals(currentCode.getCodeName()))
                && (codenameAlreadyExists(code)))
                return CODENAME_ALREADY_EXISTS;
            }

        // All checks completed
        return CODE_OK;
    }

    // check that a Code matching the name of the given Code does not already exists in current code list
    // returns true if the name is NOT in the list
    private boolean codenameAlreadyExists(@NonNull Code code) {

        if ((allcodes == null) || (allcodes.getValue() == null))
            return false;

        for (Code c : allcodes.getValue()) {
            if (c.getCodeName().equals(code.getCodeName())) {
                return true;
            }
        }
        return false; // no match found
    }
}