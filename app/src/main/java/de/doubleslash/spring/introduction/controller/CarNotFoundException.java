package de.doubleslash.spring.introduction.controller;

public class CarNotFoundException extends Exception {

    /**
     * Constructs a {@code CarNotFoundException} with {@code null}
     * as its error message string.
     */
    public CarNotFoundException() {
        super();
    }

    /**
     * Constructs a <code>CarNotFoundException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */
    public CarNotFoundException(String s) {
        super(s);
    }
}
