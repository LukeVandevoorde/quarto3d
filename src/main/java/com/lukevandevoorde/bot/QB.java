package com.lukevandevoorde.bot;

import java.util.HashSet;

public class QB {

    private byte[] rowHazards, colHazards;
    private byte eqIdxDiagHazard, compIdxDiagHazard;

    private int[] rowCounts, colCounts;
    private int eqIdxDiagCount, compIdxDiagCount;

    private byte[][] board;
    private boolean won;
    private HashSet<Byte> remainingPieces;

    private int rrow, rcol;
    private byte pieceToOffer;

    public QB () {
        rowHazards = new byte[]{-1, -1, -1, -1};
        colHazards = new byte[]{-1, -1, -1, -1};
        eqIdxDiagHazard = -1;
        compIdxDiagHazard = -1;

        rowCounts = new int[]{0, 0, 0, 0};
        colCounts = new int[]{0, 0, 0, 0};
        eqIdxDiagCount = 0;
        compIdxDiagCount = 0;

        board = new byte[4][4];
        won = false;

        remainingPieces = new HashSet<Byte>(); 
        for (int i = 0; i < 16; i++) {
            remainingPieces.add(piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0));
        }
    }

    public QB(QB other) {
        this.board = new byte[4][4];
        this.rowHazards = new byte[4];
        this.colHazards = new byte[4];
        this.rowCounts = new int[4];
        this.colCounts = new int[4];
        this.remainingPieces = new HashSet<Byte>();

        this.rowHazards[0] = other.rowHazards[0];
        this.rowHazards[1] = other.rowHazards[1];
        this.rowHazards[2] = other.rowHazards[2];
        this.rowHazards[3] = other.rowHazards[3];
        this.colHazards[0] = other.colHazards[0];
        this.colHazards[1] = other.colHazards[1];
        this.colHazards[2] = other.colHazards[2];
        this.colHazards[3] = other.colHazards[3];
        this.eqIdxDiagHazard = other.eqIdxDiagHazard;
        this.compIdxDiagHazard = other.compIdxDiagHazard;

        this.rowCounts[0] = other.rowCounts[0];
        this.rowCounts[1] = other.rowCounts[1];
        this.rowCounts[2] = other.rowCounts[2];
        this.rowCounts[3] = other.rowCounts[3];
        this.colCounts[0] = other.colCounts[0];
        this.colCounts[1] = other.colCounts[1];
        this.colCounts[2] = other.colCounts[2];
        this.colCounts[3] = other.colCounts[3];
        this.eqIdxDiagCount = other.eqIdxDiagCount;
        this.compIdxDiagCount = other.compIdxDiagCount;

        this.won = other.won;

        for (byte b: other.remainingPieces) {
            this.remainingPieces.add(b);
        }
    }

    // public Move nextMove(byte pieceToPlace) {
        
    // }

    // private float alphabeta(boolean maximizing, QB board, byte pieceToPlace, int depth, float alpha, float beta) {
    //     if (depth <= 0) return 0;
        
    //     byte hrow, hcol, hdiag = 0;
    //     if (maximizing) {
    //         float bestScore = Float.MIN_VALUE, score;
    //         int bestRow = -1, bestCol = -1;
    //         for (int row = -1; ++row < 4;) {
    //             for (int col = -1; ++col < 4;) {
    //                 if (board.board[row][col] != 0) continue;
                    
    //                 hrow = board.rowHazards[row];
    //                 hcol = board.colHazards[col];
    //                 if (row == col) hdiag = board.eqIdxDiagHazard;
    //                 else if (3 - row == col) hdiag = board.compIdxDiagHazard;
                    
    //                 // Check win
    //                 if (board.move(pieceToPlace, row, col)) {
    //                     board.undo(row, col, hrow, hcol, hdiag);
    //                     this.rrow = row;
    //                     this.rcol = col;
    //                     return 10000 + depth;
    //                 }
    //                 if (remainingPieces.size() == 0) bestScore = 0;
                    
    //                 for (byte pieceToOffer: board.remainingPieces) {
    //                     score = alphabeta(false, board, pieceToOffer, depth-1, alpha, beta);
    //                     if (score > bestScore) {
    //                         bestScore = score;
    //                         bestRow = this.rrow;
    //                         bestCol = this.rcol;
    //                     }
    //                 }
                    
    //                 board.undo(row, col, hrow, hcol, hdiag);
    //             }
    //         }
    //     } else {

    //     }
    // }

    public static byte piece(boolean isTall, boolean isLight, boolean isSquare, boolean isFilled) {
        byte p = 0;
        p |= isTall ? 1 : 2;
        p |= isLight ? 4 : 8;
        p |= isSquare ? 16 : 32;
        p |= isFilled ? 64 : 128;
        return p;
    }

    public static int pieceToID(byte piece) {
        return (piece & 1) | ((piece & 4) >> 1) | ((piece & 16) >> 2) | ((piece & 64) >> 3);
    }

    // performs absolutely 0 checks on legality and bounds
    // returns true if placing piece in the given position results in a win
    public boolean move(byte piece, int row, int col) {
        board[row][col] = piece;
        won |= ((rowHazards[row] &= piece) != 0 && (rowCounts[row] += 1) >= 4) || ((colHazards[col] &= piece) != 0 && (colCounts[col] += 1) >= 4);
        if (row == col) won |= (eqIdxDiagHazard &= piece) != 0 && (eqIdxDiagCount += 1) >= 4;
        else if (3 - row == col) won |= (compIdxDiagHazard &= piece) != 0 && (compIdxDiagCount += 1) >=4;
        remainingPieces.remove(piece);
        return won;
    }

    public void undo(int row, int col, byte hrow, byte hcol, byte hdiag) {
        remainingPieces.add(board[row][col]);
        board[row][col] = 0;
        rowHazards[row] = hrow;
        rowCounts[row] -= 1;
        colHazards[col] = hcol;
        colCounts[col] -= 1;
        if (row == col){
            eqIdxDiagHazard = hdiag;
            eqIdxDiagCount -= 1;
        } else if (3 - row == col) {
            compIdxDiagHazard = hdiag;
            compIdxDiagCount -= 1;
        }
        won = false;
    }

    public byte doIter() {
        byte r = 0;
        for (byte b: remainingPieces) {
            r |= b;
        }
        return r;
    }

    public byte get(int row, int col) {
        return board[row][col];
    }

    public byte[] getHazards(int row, int col) {
        if (row == col) {
            return new byte[]{rowHazards[row], colHazards[col], eqIdxDiagHazard};
        } else if (3 - row == col) {
            return new byte[]{rowHazards[row], colHazards[col], compIdxDiagHazard};
        }
        return new byte[]{rowHazards[row], colHazards[col]};
    }
}
