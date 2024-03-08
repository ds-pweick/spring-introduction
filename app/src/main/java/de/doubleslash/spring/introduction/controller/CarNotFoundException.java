package de.doubleslash.spring.introduction.controller;

public class CarNotFoundException extends Exception {

    /**
     * Constructs a {@code CarNotFoundException} with {@code null}
     * as its error message string.
     */
    public CarNotFoundException() {
        super();
    }

    public CarNotFoundException(String s) {
        super(s);
    }
}
