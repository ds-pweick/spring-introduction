package de.doubleslash.spring.introduction.controller;

public class CarModelOrBrandStringInvalidException extends Exception {
    /**
     * Constructs a <code>CarModelOrBrandStringTooLongException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */

    public CarModelOrBrandStringInvalidException(String s) {
        super(s);
    }
}