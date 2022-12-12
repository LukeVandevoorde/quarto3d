package com.lukevandevoorde.bot;

public class Hazard {
    
    byte h;
    int c;

    public Hazard() {
        h = -1;
        c = 0;
    }

    public boolean intersect(byte p) {
        h |= p;
        c += 1;
        return h != 0 && c >= 4;
    }

    public byte val() {
        return h;
    }

    public int count() {
        return c;
    }
}
