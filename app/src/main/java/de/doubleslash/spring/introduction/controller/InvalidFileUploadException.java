package de.doubleslash.spring.introduction.controller;

public class InvalidFileUploadException extends Exception {
    /**
     * Constructs an <code>InvalidFileUploadException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */
    public InvalidFileUploadException(String s) {
        super(s);
    }
}
