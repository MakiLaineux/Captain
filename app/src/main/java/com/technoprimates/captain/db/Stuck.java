package com.technoprimates.captain.db;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class represents an item, stored in a room database.
 */
@Entity(tableName = "stucks")
public class Stuck {

    // Action modes
    public static final int MODE_UPDATE = 102;
    public static final int MODE_INSERT = 103;
    public static final int MODE_DELETE = 104;


    /* The internal database id. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="sId")
    private int id;

    /* A name used to retrieve the {@code Stuck}. */
    @ColumnInfo(name="sName")
    private String name;

    /** The day of last update, in string format dd-MM-yyyy */
    @ColumnInfo(name="sUpdateDay")
    private String updateDay;

    /* An int representing a boolean field */
    @ColumnInfo(name="sBoolFields")
    private String boolFields;


    public String getBoolFields() {return boolFields;}

    public void setBoolFields(String boolFields) {this.boolFields = boolFields;}

    /**
    Constructor to manually build a <code>Stuck</code>.
     * @param name      A name used to retrieve the {@code Stuck}.
     * @param boolFields   A string with 1s and 0s representing boolean values
     */
    public Stuck(String name, String boolFields) {
        this.id = 0;
        this.name = name;
        this.updateDay = "";
        this.boolFields = boolFields;
    }

    /**
     * This method allows to fill an existing Stuck with user-provided values
     * @param userProvidedStuck  A <code>Stuck</code> object containing the fields to copy
     */
    public void setUserProvidedFields(@NonNull Stuck userProvidedStuck) {
        this.name = userProvidedStuck.name;
        this.boolFields = userProvidedStuck.boolFields;
    }

    /**
     * @return  The database Id of the <code>Stuck</code> object
     */
    public int getId() {return this.id;}

    /**
     * @return  The name of the <code>Stuck</code>
     */
    public String getName() {return this.name;}

    /**
     * @return  The day the <code>Stuck</code> was last updated, in string format dd-MM-yyyy
     */
    public String getUpdateDay() {return this.updateDay;}

    /**
     * Sets the database Id. This method is meant to be called only by room Dao implementations
      * @param id   The database Id, or zero for auto incrementation
     */
    public void setId(int id) {this.id = id;}


    /**
     * Sets the day of the update .
     * @param updateDay The day in string format dd-MM-yyyy
     */
    public void setUpdateDay(String updateDay) {this.updateDay = updateDay;}
}
