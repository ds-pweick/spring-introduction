package de.doubleslash.spring.introduction.controller;

public class InvalidFileRequestException extends Exception {
    /**
     * Constructs an <code>InvalidFileRequestException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */
    public InvalidFileRequestException(String s) {
        super(s);
    }
}
