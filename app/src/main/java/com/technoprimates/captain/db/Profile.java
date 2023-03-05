package com.technoprimates.captain.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * A class defining the possible usages of a statement
 *
 * A Profile object stores the usages that can be made with a Statement. It can be seen as a list of
 * booleans, each representing whether a specific condition is met.
 *
 * Specific conditions can be of three types :
 * - Nature conditions. Linked with the semantic domain of the Statement, for example "animals", "vegetals", etc.
 * A statement must be compatible with at least one nature condition
 * - Style conditions. Linked with the language style, for example "scientific", "old-style", etc.
 * A statement must be compatible with at least one style condition
 * - Criteria conditions, for example "funny", "green", "asiatic", etc. A statement must be compatible
 * with at least one nature condition
 *
 * Each Statement necessarily owns a Profile. In addition, Profile objects can be used to check if a given
 * Statement is acceptable for a specific usage. To achieve this, build a Profile object representing this specific usage,
 * and check with the matches method whether the Statement's Profile is compatible
 *
 */
public class Profile {

    public static final String TAG = "JC PROFILE";

    /**
     * Used as a prefix to store in SharedPreferences a set of booleans representing the current profile
     */
    public static final String PROFILE_BOOL = "ProfileBool";

    /**
     * The total number of specific usages in the profile
     */
    public static final int NB_CHECKBOX = 13;

    /**
     * Common prefix for the ids of the checkboxes defined in xml
     */
    public static final String CHECKBOXNAME = "checkbox";

    /* The sum of those three constants must match the total number of boolean members.
    For storage and display, the order is : nature flags, then style flags, then criteria flags */
    private static final int NB_CHECKBOX_NATURE = 3;
    private static final int NB_CHECKBOX_STYLE = 3;
    private static final int NB_CHECKBOX_CRITERIA = 7;

    /* A profile object is essentially defined by this array of booleans */
    private boolean[] mFlag = new boolean[NB_CHECKBOX];

    /**
     * Default constructor, no parameters, all usages are allowed
     */
    public Profile(){
        for (int i = 0; i < NB_CHECKBOX; i++)
            this.mFlag[i] = true;
    }

    /**
     * Constructor setting allowed usages using values passed in a array
     * @param boolArray an array of NB_CHECKBOX booleans, pass "true" to allow each usage
     */
    public Profile(boolean[] boolArray){
        this.mFlag = boolArray;
    }

    /**
     * Returns a string of fixed length. Each char represent a boolean member of the object.
     *
     * @return a string of length NB_CHECKBOX whose chars represent each a condition. The char is
     * 'X' if the condition is met, ' ' otherwise.
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(NB_CHECKBOX);
        for (int i =0 ; i < NB_CHECKBOX ; i++)
            buffer.append(mFlag[i] ? 'X' : ' ');

        Log.d(TAG, "toString : Text : Z" + buffer + "Z");

        return(buffer.toString());
    }

    /**
     * Checks whether a specific usage condition is allowed
     * @param i number of the usage to check
     * @return true if the usage is allowed
     */
    public boolean isEnabled(int i){return mFlag[i];}

    /**
     * Updates a usage
     * @param i number of the specific usage condition to set
     * @param b true if this specific usage is allowed
     */
    public void setEnabled(int i, boolean b){mFlag[i]=b;}

    /**
     * Set a Profile's content by updating all its possible usages.
     * @param s a string of length NB_CHECKBOX whose chars represent each a usage.
     *          The char is 'X' if the usage is allowed, ' ' otherwise.
     *          If the length of the argument differs from NB_CHECKBOX, a
     *          ProfileFormatException is thrown
     */
    void feedFromString (String s) throws ProfileFormatException{
            if ((s == null) || (s.length() != NB_CHECKBOX)) {
                throw new ProfileFormatException();
            }
            for (int i = 0; i < NB_CHECKBOX; i++)
                this.mFlag[i] = (s.charAt(i) == 'X');
    }

    /**
     * Reset the profile, all usages are set to "allowed"
     */
    public void clear () {
        for (int i = 0; i < NB_CHECKBOX; i++)
            this.mFlag[i] = true;
    }

    /**
     * Read the user preferences for the profile, and copy them into the current Profile object.
     * The user preferences are stored in SharedPreferences
     * @param context a context to access the SharedPreferences
     */
    public void feedFromPreferences(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            String prefName = PROFILE_BOOL + i;
            mFlag[i] = sharedPref.getBoolean(prefName, true);
        }
    }


    /**
     * Checks whether the specified Profile is compatible with the current Profile object.
     * Two profiles are compatible if they both share at least one common nature,
     * at least one common style, and at least one common criteria
     *
     * @param p Profile whose compatibility is to be checked
     * @return true if both Profile are compatible
     */
    public boolean matches(Profile p){
        // returns true if natures match, styles also match, and criterias also match

        int j=0;

        // at least one nature must match
        boolean match = false;
        for (int i =0 ; i < NB_CHECKBOX_NATURE ; i++) {
            if (mFlag[j] && p.mFlag[j]) match = true;
            j++;
        }
        if (!match) return false;


        // at least one style must match
        match = false;
        for (int i =0 ; i < NB_CHECKBOX_STYLE ; i++) {
            if (mFlag[j] && p.mFlag[j]) match = true;
            j++;
        }
        if (!match) return false;

        // at least one criteria must match
        match = false;
        for (int i =0 ; i < NB_CHECKBOX_CRITERIA ; i++) {
            if (mFlag[j] && p.mFlag[j]) match = true;
            j++;
        }
        return match;
    }
}

