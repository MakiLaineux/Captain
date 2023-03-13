package com.technoprimates.captain.db;

import android.util.Log;

import androidx.annotation.NonNull;

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
     * The total number of booleans in the profile
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
    private final boolean[] mFlag = new boolean[NB_CHECKBOX];

    /**
     * Default constructor, builds a default Profile with all usages allowed
     */
    public Profile(){
        for (int i = 0; i < NB_CHECKBOX; i++) this.mFlag[i] = true;
    }

    /**
     * Constructor from a String.
     * If the String does not define a valid Profile, the default Profile is built
     */
    public Profile(String stringProfile){
        this(); // initialize to default Profile
        // if the string is valid, set the booleans
        if ((stringProfile != null)
                && (stringProfile.length() == NB_CHECKBOX)
                && (isValidProfileString(stringProfile))) {
            for (int i = 0; i < NB_CHECKBOX; i++) this.mFlag[i] = (stringProfile.charAt(i) == 'X');
        }
    }


    /**
     * Static method checking if a String defines a valid Profile
     * @param stringProfile The String to check
     * @return true if it defines a valid Profile
     */
    public static boolean isValidProfileString(String stringProfile) {
        boolean[] flags = new boolean[NB_CHECKBOX];


        // split the string into a boolean array
        for (int i = 0; i < NB_CHECKBOX; i++)
            flags[i] = (stringProfile.charAt(i) == 'X');

        // check the booleans
        // at least one nature must be true
        int j=0; // loop index on all booleans
        boolean bValid = false;
        for (int i =0 ; i < NB_CHECKBOX_NATURE ; i++)
            if (flags[j++]) bValid = true;
        if (!bValid) return false;

        // at least one style must be true
        bValid = false;
        for (int i =0 ; i < NB_CHECKBOX_STYLE ; i++)
            if (flags[j++]) bValid = true;
        if (!bValid) return false;

        // at least one criteria must be true
        bValid = false;
        for (int i =0 ; i < NB_CHECKBOX_CRITERIA ; i++)
            if (flags[j++]) bValid = true;
        return bValid;
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
     * Set a Profile's content by updating all its possible usages.
     * @param s a string of length NB_CHECKBOX whose chars represent each a usage.
     *          The char is 'X' if the usage is allowed, ' ' otherwise.
     */
    public void feedFromString (String s) {
        for (int i = 0; i < NB_CHECKBOX; i++)
            this.mFlag[i] = (s.charAt(i) == 'X');
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

