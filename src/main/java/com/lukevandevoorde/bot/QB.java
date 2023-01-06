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

        remainingPieces = QuartoPiece.allBytes();
    }

    public QB(QB other) {
        this.board = new byte[4][4];
        this.rowHazards = new byte[4];
        this.colHazards = new byte[4];
        this.rowCounts = new int[4];
        this.colCounts = new int[4];
        this.remainingPieces = new HashSet<Byte>();

        for (int i = 0; i < 4; i++) {
            this.rowHazards[i] = other.rowHazards[i];
            this.colHazards[i] = other.colHazards[i];
            this.rowCounts[i] = other.rowCounts[i];
            this.colCounts[i] = other.colCounts[i];
        }
        this.eqIdxDiagHazard = other.eqIdxDiagHazard;
        this.compIdxDiagHazard = other.compIdxDiagHazard;
        this.eqIdxDiagCount = other.eqIdxDiagCount;
        this.compIdxDiagCount = other.compIdxDiagCount;

        this.won = other.won;

        for (byte b: other.remainingPieces) {
            this.remainingPieces.add(b);
        }
    }

    public boolean won() {
        return this.won;
    }

    // performs absolutely 0 checks on legality and bounds
    // returns true if placing piece in the given position results in a win
    public boolean move(byte piece, int row, int col) {
        board[row][col] = piece;

        boolean rowHazardous = (rowHazards[row] &= piece) != 0;
        boolean rowFull = (rowCounts[row] += 1) >= 4;
        boolean colHazardous = (colHazards[col] &= piece) != 0;
        boolean colFull = (colCounts[col] += 1) >= 4;
        won |= (rowFull && rowHazardous) || (colFull && colHazardous);

        if (row == col) {
            boolean diagHazardous = (eqIdxDiagHazard &= piece) != 0;
            boolean diagFull = (eqIdxDiagCount += 1) >= 4;
            won |= diagFull && diagHazardous;
        } else if (3 - row == col) {
            boolean diagHazardous = (compIdxDiagHazard &= piece) != 0;
            boolean diagFull = (compIdxDiagCount += 1) >= 4;
            won |= diagFull && diagHazardous;
        }

        remainingPieces.remove(piece);
        return won;
    }

    // if not a diag, prevDiagHazard is disregarded
    public void undo(int row, int col, byte prevRowHazard, byte prevColHazard, byte prevDiagHazard) {
        remainingPieces.add(board[row][col]);
        board[row][col] = 0;
        rowHazards[row] = prevRowHazard;
        rowCounts[row] -= 1;
        colHazards[col] = prevColHazard;
        colCounts[col] -= 1;
        won = false;
        if (row == col){
            eqIdxDiagHazard = prevDiagHazard;
            eqIdxDiagCount -= 1;
        } else if (3 - row == col) {
            compIdxDiagHazard = prevDiagHazard;
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
