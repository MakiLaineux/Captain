package com.technoprimates.captain;

import android.app.Application;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.technoprimates.captain.db.Profile;
import com.technoprimates.captain.db.Stueck;
import com.technoprimates.captain.db.StueckRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * An application-scoped ViewModel managing Stueck objects
 */
public class StueckViewModel extends AndroidViewModel {

    public static final String TAG = "STUECKVIEWMODEL";

    // Values returned by business logic checkings
    public static final int STUECK_OK = 101;
    public static final int NO_STUECK = 102;
    public static final int NO_STUECK_NAME = 103;
    public static final int STUECK_NAME_ALREADY_EXISTS = 104;
    public static final int INVALID_STUECK_BOOLEANS = 105;

    // Stueck repository
    private final StueckRepository repository;

    // A LiveData list of all Stücks, to be observed to update the RecylerView
    private final LiveData<List<Stueck>> allStuecksList;

    // A list of Stücks matching the current Profile,
    // This is the list displayed in the recyclerview
    // and used when requesting profiled Stücks
    private final List<Stueck> profiledStuecksList;

    // TODO
    private final List<String> nextNames = new ArrayList<>();


    // The nature of the next action to be processed
    private int actionMode;

    // A reference to the currently processed Stueck. This is set via the setNextCodeAction method
    private Stueck currentStueck;

    // Current profile for stuecks
    private final Profile currentProfile;

    /**
     * Constructor of the ViewModel managing <code>Stueck</code> objects.
     * <p>Do NOT call this directly, use ViewModelProvider instead. </p>
     * @param application   The current application
     */
    public StueckViewModel(Application application) {
        super(application);
        /* Initialize the current Profile. If possible, the current Profile is defined from the values stored as a String in the Shared Preferences.
         If no values are stored, or if the stored values are not ok, the current Profile is set to a default Profile.
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(application);
        currentProfile = new Profile(sharedPref.getString("Profile", null));

        // Initialize the Stueck repository and the list of stuecks matching the current profile
        repository = new StueckRepository(application);
        allStuecksList = repository.getAllStuecksList();
        profiledStuecksList = new ArrayList<>();
        //updateProfiledStuecksList();

        // Initalize current Stueck
        currentStueck = null;
   }

    /**
     * Change the current profile.
     * @param profileString The new profile stored in a String
     * @return false if the provided string does not define a valid Profile, in which case the current Profile is left unchanged
     */
    public boolean setProfile(String profileString) {
        if (Profile.isValidProfileString(profileString)) {
            currentProfile.feedFromString(profileString);
            // save the string defining the new profile in shared preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("Profile", profileString);
            editor.apply();

            // update profiled stuecks list
            updateProfiledStuecksList();

            return true;
        } else {
            return false;
        }
    }


    /*
    Rebuild the profiled Stuecks list from the livedata list of all stücks, selecting items matching current Profile
    And rebuild the list of next stüecks names
    Call this when Profile is changed or when livedata changes
     */
    public void updateProfiledStuecksList() {
        profiledStuecksList.clear();
        nextNames.clear();
        // livedata list must have been set
        assert (allStuecksList.getValue() != null);
        for (int i=0; i < allStuecksList.getValue().size(); i++) {
            if (getProfile().matches(allStuecksList.getValue().get(i).getProfile())) {
                profiledStuecksList.add(allStuecksList.getValue().get(i));
                nextNames.add(allStuecksList.getValue().get(i).getName());
            }
        }
    }

    /**
     * Deletes the currently selected code
     */
    public void deleteStueck() {
        repository.deleteStueck(currentStueck.getId());
    }

    /**
     * Set the next operation to perform
     * <p>Possible values for the parameters are :</p>
     * <li>Stueck.MODE_UPDATE, with new content of the Stueck to update</li>
     * <li>Stueck.MODE_INSERT, with  <code>null</code></li>
     * <li>Stueck.MODE_DELETE, with Stueck to delete</li>
     *
     * @param operation    The action to be performed
     */
    public void selectActionToProcess(int operation)
    {
        actionMode = operation;
    }

    /**
     * Set the <code>Stueck</code> object to process in the next operation.
     * @param stueck      A <code>Stueck</code> object already existing in the database.
     */
    public void selectStueckToProcess(Stueck stueck)
    {
        currentStueck = stueck;
    }

    /**
     * Set the UI Fields of the <code>Stueck</code> object to process in the next operation.
     * @param stueck      A <code>Stueck</code> object containing the user-provided fields.
     */
    public void fillStueckToProcess(@NonNull Stueck stueck)
    {
        if (currentStueck == null) {
            Log.e(TAG, "No currentStueck set");
            return;
        }
        currentStueck.setUserProvidedFields(stueck);
    }

    /**
     * Update a Stueck.
     */
    public void updateStueck() {
        // before updating, set the UpdateDay value in format dd-MM-yyyy
        currentStueck.setUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.updateStueck(currentStueck);
    }

    /**
     * Insert a new <code>Stueck</code>.
     * <p>The <code>Stueck</code> to insert must have been set </p>
     */
    public void insertStueck() {
        /*
        The Stueck to insert must have been set in the currentItem member
        The codeId value is zero which leads to autoincrementation in the room database.
        The updateDay value is set to the day of the current system Date
         */
        // set the UpdateDay value in format dd-MM-yyyy
        currentStueck.setUpdateDay(DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
        repository.insertStueck(currentStueck);
    }

    /**
     * Reinsert the <code>Stueck</code> that was just deleted.
     * Call this method if the user cancels the deletion of a Stueck. The <code>Stueck</code> will be inserted with all his previous fields,
     * and it will keep its previous previous dbId and updateDay values
     */
    public void reInsertStueck() {
        repository.insertStueck(currentStueck);
    }


    public int checkStueckBusinessLogic(Stueck stueck, int actionMode) {
        // The stueck must not be null
        if (stueck == null)
            return NO_STUECK;

        // Refuse insertion with empty or already existing stueck name
        if (actionMode == Stueck.MODE_INSERT) {
            if (stueck.getName().equals(""))
                return NO_STUECK_NAME;
            if (stueckNameAlreadyExists(stueck)) {
                return STUECK_NAME_ALREADY_EXISTS;
            }
        }

        // Refuse update if the stueck name provided is changed
        // to an already existing stueck name
        if (actionMode == Stueck.MODE_UPDATE) {
            if ((!stueck.getName().equals(currentStueck.getName()))
                && (stueckNameAlreadyExists(stueck)))
                return STUECK_NAME_ALREADY_EXISTS;
            }

        // check profile validity
        if (!Profile.isValidProfileString(stueck.getBoolFields())) {
            return INVALID_STUECK_BOOLEANS;
        }

        // All checks completed
        return STUECK_OK;
    }

    // check that a Stueck matching the name of the given Stueck does not already exists in current stueck list
    // returns true if the name is NOT in the list
    private boolean stueckNameAlreadyExists(@NonNull Stueck stueck) {

        if ((allStuecksList == null) || (allStuecksList.getValue() == null))
            return false;

        for (Stueck c : allStuecksList.getValue()) {
            if (c.getName().equals(stueck.getName())) {
                return true;
            }
        }
        return false; // no match found
    }

    public List<String> getNextNames() {
        return nextNames;
    }

    public String popNextName(int pos) {
        try {
            return (nextNames.remove(pos));
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "pop String : No string at this pos");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "pop String : unexpected exception");
            return null;
        }
    }

    public int nbNextNames() {
        return nextNames.size();
    }

    public void rebuildNextnames() {
        nextNames.clear();
        for (int i=0; i < profiledStuecksList.size(); i++) {
                nextNames.add(profiledStuecksList.get(i).getName());
            }
    }

    /**
     * Get the current profile
     * @return  A profile stored in a String
     */
    public Profile getProfile() {
        return currentProfile;
    }


    /**
     * Get a LiveData list of all <code>Stueck</code> objects in database matching the current Profile
     * <p>The return value is to be observed to update the RecyclerView </p>
     * @return  The LiveData list of Stuecks to display
     */
    public LiveData<List<Stueck>> getAllStuecksList() {return allStuecksList;}


    /**
     * Get the next action to be performed.
     * <p>Possible values are :</p>
     * <li>Stueck.MODE_VISU</li>
     * <li>Stueck.MODE_UPDATE</li>
     * <li>Stueck.MODE_INSERT</li>
     * <li>Stueck.MODE_DELETE</li>
     *
     * @return  The action to be performed
     */
    public int getActionMode() {
        return actionMode;
    }

    /**
     * Get db-existing Stueck to process next.
     * <p>The Stueck to process must have been set previously with the <code>setNextCodeAction</code> method</p>
     * @return  A <code>Stueck</code> object already existing in the database, or null if no Stueck object was previously set.
     */
    public Stueck getStueckToProcess() {
        return currentStueck;
    }

    public List<Stueck> getProfiledStuecksList() {return profiledStuecksList;}



}