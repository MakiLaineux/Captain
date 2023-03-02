package com.technoprimates.captain.db;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class represents a code, aka a key, stored in a room database.
 */
@Entity(tableName = "codes")
public class Code {

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
    private int codeId;

    /* A name used to retrieve the {@code Code}. */
    @ColumnInfo(name="cName")
    private String codeName;

    /* The value of the key represented by the {@code Code} object. */
    @ColumnInfo(name="cValue")
    private String codeValue;

    /* A category name stored in string format */
    @ColumnInfo(name="cCategory")
    private String codeCategory;

    /* Free optional comments that content additional description of the {@code Code},
     * like a username, an address, or a client id. */
    @ColumnInfo(name="cComments")
    private String codeComments;

    /** The day of last update, in string format dd-MM-yyyy */
    @ColumnInfo(name="cUpdateDay")
    private String codeUpdateDay;

    /* An int representing the fingerprint protection mode */
    @ColumnInfo(name="cProtectMode")
    private int codeProtectMode;


    /**
    Constructor to manually build a <code>Code</code>.
     * @param codeName      A name used to retrieve the {@code Code}.
     * @param codeValue     The value of the key represented by the {@code Code} object.
     * @param codeCategory  A category name stored in string format
     * @param codeComments  Free optional comments that content additional description of the {@code Code}, like a username, an address, or a client id.
     * @param codeProtectMode   The fingerprint protection mode, {@code FINGERPRINT_PROTECTED} or {@code NOT_FINGERPRINT_PROTECTED}
     */
    public Code(String codeName, String codeValue, String codeCategory, String codeComments, int codeProtectMode) {
        this.codeId = 0;
        this.codeName = codeName;
        this.codeValue=codeValue;
        this.codeCategory = codeCategory;
        this.codeComments = codeComments;
        this.codeUpdateDay = "";
        this.codeProtectMode = codeProtectMode;
    }

    /**
     * This method allows to update an existing Code retrieved from the database with UI fields provided in the Code passed.
     * @param userProvidedCode  A <code>Code</code> object containing the fields to copy
     */
    public void setUserProvidedFields(@NonNull Code userProvidedCode) {
        this.codeName = userProvidedCode.codeName;
        this.codeValue = userProvidedCode.codeValue;
        this.codeCategory = userProvidedCode.codeCategory;
        this.codeComments = userProvidedCode.codeComments;
        this.codeProtectMode = userProvidedCode.codeProtectMode;
    }

    /**
     * @return  The database Id of the <code>Code</code> object
     */
    public int getCodeId() {return this.codeId;}

    /**
     * @return  The name of the <code>Code</code>
     */
    public String getCodeName() {return this.codeName;}

    /**
     * @return  The value of the <code>Code</code>
     */
    public String getCodeValue() {return this.codeValue;}

    /**
     * @return  The category of the <code>Code</code>
     */
    public String getCodeCategory() {return this.codeCategory;}

    /**
     * @return  The comments associated with the <code>Code</code>
     */
    public String getCodeComments() {return this.codeComments;}

    /**
     * @return  The day the <code>Code</code> was last updated, in string format dd-MM-yyyy
     */
    public String getCodeUpdateDay() {return this.codeUpdateDay;}

    /**
     * @return  The The fingerprint protection mode, may be {@code FINGERPRINT_PROTECTED} or {@code NOT_FINGERPRINT_PROTECTED}
     */
    public int getCodeProtectMode() {return this.codeProtectMode;}

    /**
     * Sets the database Id. This method is meant to be called only by room Dao implementations
      * @param codeId   The database Id, or zero for auto incrementation
     */
    public void setCodeId(int codeId) {this.codeId = codeId;}


    /**
     * Sets the day of the update .
     * @param codeUpdateDay The day in string format dd-MM-yyyy
     */
    public void setCodeUpdateDay(String codeUpdateDay) {this.codeUpdateDay = codeUpdateDay;}

}
