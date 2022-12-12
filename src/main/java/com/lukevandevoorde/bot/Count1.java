package com.lukevandevoorde.bot;

public class Count1 {
    private static final byte[] pieces;
    static {
        pieces = new byte[16];
        for (int i = 0; i < 16; i++) {
            pieces[i] = (byte)(127 & QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0));
        }
    }
    private boolean[] remainingPieces;

    public Count1() {
        remainingPieces = new boolean[128];
        for (int i = 0; i < remainingPieces.length; i++) {
            remainingPieces[i] = false;
        }

        for (int i = 0; i < 16; i++) {
            remainingPieces[127 & QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0)] = true;
        }
    }

    public void move(byte p) {
        remainingPieces[127 & p] = false;
    }

    public int smartCount() {
        int r = 0;
        for (int i = -1; ++i < 16;) {
            if (remainingPieces[pieces[i]]) {
                r += 1;
            }
        }
        return r;
    }

    public int stupidCount() {
        int r = 0;
        for (int i = -1; ++i < 128;) {
            if (remainingPieces[pieces[i]]) {
                r += 1;
            }
        }
        return r;
    }
}
