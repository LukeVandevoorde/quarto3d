package com.lukevandevoorde.bot;

import java.util.HashSet;

public class QB_Arr {

    private Hazard[][][] hazards;
    private byte[][] board;
    private HashSet<Byte> remainingPieces;
    private boolean won;

    public QB_Arr () {
        Hazard[] rowHazards = new Hazard[]{new Hazard(), new Hazard(), new Hazard(), new Hazard()};
        Hazard[] colHazards = new Hazard[]{new Hazard(), new Hazard(), new Hazard(), new Hazard()};
        Hazard d1 = new Hazard();
        Hazard d2 = new Hazard();
        
        hazards = new Hazard[4][4][];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    hazards[i][j] = new Hazard[]{rowHazards[i], colHazards[j], d1};
                } else if (3 - i == j) {
                    hazards[i][j] = new Hazard[]{rowHazards[i], colHazards[j], d2};
                } else {
                    hazards[i][j] = new Hazard[]{rowHazards[i], colHazards[j]};
                }
            }
        }

        board = new byte[4][4];
        remainingPieces = new HashSet<Byte>(); 
        for (int i = 0; i < 16; i++) {
            remainingPieces.add(piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0));
        }

        won = false;
    }

    public boolean move(byte piece, int row, int col) {
        board[row][col] = piece;
        for (Hazard h: hazards[row][col]) {
            won |= h.intersect(piece);
        }
        remainingPieces.remove(piece);
        return won;
    }

    // public HashSet<Byte> remainings

    public static byte piece(boolean isTall, boolean isLight, boolean isSquare, boolean isFilled) {
        byte p = 0;
        p |= isTall ? 1 : 2;
        p |= isLight ? 4 : 8;
        p |= isSquare ? 16 : 32;
        p |= isFilled ? 64 : 128;
        return p;
    }
}
