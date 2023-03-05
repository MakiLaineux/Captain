package com.technoprimates.captain.db;

/**
 * Custom exception
 * Nothing special with the code
 */

public class ProfileFormatException extends Exception {
    // Parameterless Constructor
    public ProfileFormatException() {}

    // Constructor that accepts a message
    public ProfileFormatException(String message)
    {
        super(message);
    }
}
