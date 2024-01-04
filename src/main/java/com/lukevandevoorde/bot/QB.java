package com.lukevandevoorde.bot;

import java.util.HashSet;

import com.lukevandevoorde.quartolayer.QuartoPiece;

public class QB {

    protected byte[] rowHazards, colHazards;
    protected byte eqIdxDiagHazard, compIdxDiagHazard;

    protected int[] rowCounts, colCounts;
    protected int eqIdxDiagCount, compIdxDiagCount;

    protected byte[][] board;
    protected boolean won;
    protected HashSet<Byte> remainingPieces;

    private int turnIndex;
    private byte[] prevRowHaz, prevColHaz, prevEqIdxDiagHaz, prevCompIdxDiagHaz;
    private int[] prevRow, prevCol;

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

        turnIndex = 0;
        prevRowHaz = new byte[16]; prevRowHaz[0] = -1;
        prevColHaz = new byte[16]; prevColHaz[0] = -1;
        prevEqIdxDiagHaz = new byte[16]; prevEqIdxDiagHaz[0] = -1;
        prevCompIdxDiagHaz = new byte[16]; prevCompIdxDiagHaz[0] = -1;
        prevRow = new int[16];
        prevCol = new int[16];

        remainingPieces = QuartoPiece.allBytes();
    }

    @SuppressWarnings("unchecked")
    public QB(QB other) {
        this.board = new byte[4][4];
        this.rowHazards = new byte[4];
        this.colHazards = new byte[4];
        this.rowCounts = new int[4];
        this.colCounts = new int[4];
        this.remainingPieces = (HashSet<Byte>) other.remainingPieces.clone();

        for (int i = 0; i < 4; i++) {
            this.rowHazards[i] = other.rowHazards[i];
            this.colHazards[i] = other.colHazards[i];
            this.rowCounts[i] = other.rowCounts[i];
            this.colCounts[i] = other.colCounts[i];
            this.board[i][0] = other.board[i][0];
            this.board[i][1] = other.board[i][1];
            this.board[i][2] = other.board[i][2];
            this.board[i][3] = other.board[i][3];
        }
        this.eqIdxDiagHazard = other.eqIdxDiagHazard;
        this.compIdxDiagHazard = other.compIdxDiagHazard;
        this.eqIdxDiagCount = other.eqIdxDiagCount;
        this.compIdxDiagCount = other.compIdxDiagCount;

        this.turnIndex = other.turnIndex;
        this.prevRowHaz = new byte[16];
        this.prevColHaz = new byte[16];
        this.prevEqIdxDiagHaz = new byte[16];
        this.prevCompIdxDiagHaz = new byte[16];
        this.prevRow = new int[16];
        this.prevCol = new int[16];

        for (int i = 0; i < 16; i++) {
            this.prevRowHaz[i] = other.prevRowHaz[i];
            this.prevColHaz[i] = other.prevColHaz[i];
            this.prevEqIdxDiagHaz[i] = other.prevEqIdxDiagHaz[i];
            this.prevCompIdxDiagHaz[i] = other.prevCompIdxDiagHaz[i];
            this.prevRow[i] = other.prevRow[i];
            this.prevCol[i] = other.prevCol[i];
        }

        this.won = other.won;
    }

    public boolean won() {
        return this.won;
    }

    // performs absolutely 0 checks on legality and bounds
    // returns true if placing piece in the given position results in a win
    public boolean move(byte piece, int row, int col) {
        if (board[row][col] != 0) throw new IllegalArgumentException("Piece already here");
        if (!remainingPieces.contains(piece)) throw new IllegalArgumentException("Piece already played");
        board[row][col] = piece;

        prevRow[turnIndex] = row;
        prevCol[turnIndex] = col;

        prevRowHaz[turnIndex] = rowHazards[row];
        boolean rowHazardous = (rowHazards[row] &= piece) != 0;
        boolean rowFull = (rowCounts[row] += 1) >= 4;
        prevColHaz[turnIndex] = colHazards[col];
        boolean colHazardous = (colHazards[col] &= piece) != 0;
        boolean colFull = (colCounts[col] += 1) >= 4;
        won |= (rowFull && rowHazardous) || (colFull && colHazardous);

        if (row == col) {
            prevEqIdxDiagHaz[turnIndex] = eqIdxDiagHazard;
            boolean diagHazardous = (eqIdxDiagHazard &= piece) != 0;
            boolean diagFull = (eqIdxDiagCount += 1) >= 4;
            won |= diagFull && diagHazardous;
        } else if (3 - row == col) {
            prevCompIdxDiagHaz[turnIndex] = compIdxDiagHazard;
            boolean diagHazardous = (compIdxDiagHazard &= piece) != 0;
            boolean diagFull = (compIdxDiagCount += 1) >= 4;
            won |= diagFull && diagHazardous;
        }

        remainingPieces.remove(piece);
        turnIndex += 1;
        return won;
    }

    public void undo() {
        turnIndex -= 1;
        int row = prevRow[turnIndex];
        int col = prevCol[turnIndex];
        remainingPieces.add(board[row][col]);
        board[row][col] = 0;
        rowHazards[row] = prevRowHaz[turnIndex];
        rowCounts[row] -= 1;
        colHazards[col] = prevColHaz[turnIndex];
        colCounts[col] -= 1;
        won = false;
        
        if (row == col) {
            eqIdxDiagHazard = prevEqIdxDiagHaz[turnIndex];
            eqIdxDiagCount -= 1;
        } else if (3 - row == col) {
            compIdxDiagHazard = prevCompIdxDiagHaz[turnIndex];
            compIdxDiagCount -= 1;
        }
    }

    public byte pieceAt(int row, int col) {
        return board[row][col];
    }

    public HashSet<Byte> remainingPieces() {
        return this.remainingPieces;
    }

    // [row hazard, col hazard, {diag hazard}]
    public byte[] getHazards(int row, int col) {
        if (row == col) {
            return new byte[]{rowHazards[row], colHazards[col], eqIdxDiagHazard};
        } else if (3 - row == col) {
            return new byte[]{rowHazards[row], colHazards[col], compIdxDiagHazard};
        }
        return new byte[]{rowHazards[row], colHazards[col]};
    }

    // [row count, col count, {diag count}]
    public int[] getCounts(int row, int col) {
        if (row == col) {
            return new int[]{rowCounts[row], colCounts[col], eqIdxDiagCount};
        } else if (3 - row == col) {
            return new int[]{rowCounts[row], colCounts[col], compIdxDiagCount};
        }
        return new int[]{rowCounts[row], colCounts[col]};
    }
}
