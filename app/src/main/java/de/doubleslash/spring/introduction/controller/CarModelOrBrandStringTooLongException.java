package de.doubleslash.spring.introduction.controller;

public class CarModelOrBrandStringTooLongException extends Exception {
    /**
     * Constructs a <code>CarModelOrBrandStringTooLongException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     */

    public CarModelOrBrandStringTooLongException(String s) {
        super(s);
    }
}