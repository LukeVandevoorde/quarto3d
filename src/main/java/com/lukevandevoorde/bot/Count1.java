package com.lukevandevoorde.bot;

public class Count1 {
    private static final byte[] pieces;
    private static final byte[] PIECES;
    static {
        pieces = new byte[16];
        PIECES = new byte[16];
        for (int i = 0; i < 16; i++) {
            PIECES[i] = QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0);
            pieces[i] = (byte)(127 & QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0));
        }
    }
    private boolean[] remainingPieces;
    private boolean[] avail;

    public Count1() {
        remainingPieces = new boolean[128];
        for (int i = 0; i < remainingPieces.length; i++) {
            remainingPieces[i] = false;
        }

        for (int i = 0; i < 16; i++) {
            remainingPieces[127 & QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0)] = true;
        }

        avail = new boolean[16];
        for (int i = 0; i < 16; i++) {
            avail[i] = true;
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
            if (remainingPieces[i]) {
                r += 1;
            }
        }
        return r;
    }

    public int smartestMaybeCount() {
        int r = 0;
        for (int i = 0; i < 16; i++) {
            if (avail[QB.pieceToID(PIECES[i])]) {
                r += 1;
            }
        }
        return r;
    }
}
