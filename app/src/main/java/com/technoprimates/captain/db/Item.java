package com.technoprimates.captain.db;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class represents an item, stored in a room database.
 */
@Entity(tableName = "items")
public class Item {

    // Action modes
    public static final int MODE_VISU = 101;
    public static final int MODE_UPDATE = 102;
    public static final int MODE_INSERT = 103;
    public static final int MODE_DELETE = 104;

    // Fingerprint modes
    public static final int FINGERPRINT_PROTECTED = 201;
    public static final int NOT_FINGERPRINT_PROTECTED = 202;

    /* The internal database id. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="cId")
    private int itemId;

    /* A name used to retrieve the {@code Item}. */
    @ColumnInfo(name="cName")
    private String itemName;

    /* The value of the key represented by the {@code Item} object. */
    @ColumnInfo(name="cValue")
    private String itemValue;

    /* A category name stored in string format */
    @ColumnInfo(name="cCategory")
    private String itemCategory;

    /* Free optional comments that content additional description of the {@code Item},
     * like a username, an address, or a client id. */
    @ColumnInfo(name="cComments")
    private String itemComments;

    /** The day of last update, in string format dd-MM-yyyy */
    @ColumnInfo(name="cUpdateDay")
    private String itemUpdateDay;

    /* An int representing the fingerprint protection mode */
    @ColumnInfo(name="cProtectMode")
    private int itemProtectMode;


    /**
    Constructor to manually build a <code>Item</code>.
     * @param itemName      A name used to retrieve the {@code Item}.
     * @param itemValue     The value of the key represented by the {@code Item} object.
     * @param itemCategory  A category name stored in string format
     * @param itemComments  Free optional comments that content additional description of the {@code Item}, like a username, an address, or a client id.
     * @param itemProtectMode   The fingerprint protection mode, {@code FINGERPRINT_PROTECTED} or {@code NOT_FINGERPRINT_PROTECTED}
     */
    public Item(String itemName, String itemValue, String itemCategory, String itemComments, int itemProtectMode) {
        this.itemId = 0;
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.itemCategory = itemCategory;
        this.itemComments = itemComments;
        this.itemUpdateDay = "";
        this.itemProtectMode = itemProtectMode;
    }

    /**
     * This method allows to update an existing Item retrieved from the database with UI fields provided in the Item passed.
     * @param userProvidedItem  A <code>Item</code> object containing the fields to copy
     */
    public void setUserProvidedFields(@NonNull Item userProvidedItem) {
        this.itemName = userProvidedItem.itemName;
        this.itemValue = userProvidedItem.itemValue;
        this.itemCategory = userProvidedItem.itemCategory;
        this.itemComments = userProvidedItem.itemComments;
        this.itemProtectMode = userProvidedItem.itemProtectMode;
    }

    /**
     * @return  The database Id of the <code>Item</code> object
     */
    public int getItemId() {return this.itemId;}

    /**
     * @return  The name of the <code>Item</code>
     */
    public String getItemName() {return this.itemName;}

    /**
     * @return  The value of the <code>Item</code>
     */
    public String getItemValue() {return this.itemValue;}

    /**
     * @return  The category of the <code>Item</code>
     */
    public String getItemCategory() {return this.itemCategory;}

    /**
     * @return  The comments associated with the <code>Item</code>
     */
    public String getItemComments() {return this.itemComments;}

    /**
     * @return  The day the <code>Item</code> was last updated, in string format dd-MM-yyyy
     */
    public String getItemUpdateDay() {return this.itemUpdateDay;}

    /**
     * @return  The The fingerprint protection mode, may be {@code FINGERPRINT_PROTECTED} or {@code NOT_FINGERPRINT_PROTECTED}
     */
    public int getItemProtectMode() {return this.itemProtectMode;}

    /**
     * Sets the database Id. This method is meant to be called only by room Dao implementations
      * @param itemId   The database Id, or zero for auto incrementation
     */
    public void setItemId(int itemId) {this.itemId = itemId;}


    /**
     * Sets the day of the update .
     * @param itemUpdateDay The day in string format dd-MM-yyyy
     */
    public void setItemUpdateDay(String itemUpdateDay) {this.itemUpdateDay = itemUpdateDay;}

}
