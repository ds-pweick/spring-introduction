package de.doubleslash.spring.introduction.controller;

public class CarModelOrBrandStringTooLongException extends Exception {
    /**
     * Constructs a {@code CarModelOrBrandStringTooLong} with {@code null}
     * as its error message string.
     */

    public CarModelOrBrandStringTooLongException(String s) {
        super(s);
    }
}