package de.doubleslash.spring.introduction.controller;

public class CarModelAndOrBrandStringInvalidException extends Exception {
    /**
     * Constructs a <code>CarModelOrBrandStringTooLongException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */

    public CarModelAndOrBrandStringInvalidException(String s) {
        super(s);
    }
}