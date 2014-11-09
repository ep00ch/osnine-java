package org.roug.osnine;

/**
 * Word size register.
 */
public class Word extends Register {

    @Override
    public int get() {
        return value & 0xffff;
    }

    @Override
    public void set(int newValue) throws IllegalArgumentException {
        if (newValue > 0xffff || newValue < -0x8000) {
            throw new IllegalArgumentException("Value must fit in 16 bits.");
        }
        value = newValue;
    }

    @Override
    public int getSigned() {
        if (value < 0x8000) {
            return value;
        } else {
            return -((~value & 0x7fff) + 1);
        }
    }
}

