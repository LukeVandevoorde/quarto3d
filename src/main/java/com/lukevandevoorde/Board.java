package com.lukevandevoorde;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.lukevandevoorde.bot.QB;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class Board implements QuartoBoardState {

    private QB qb;
    private HashSet<QuartoPiece> remainingPieces;

    public Board() {
        this.qb = new QB();
        this.remainingPieces = new HashSet<QuartoPiece>();
        for (Byte b: qb.remainingPieces()) {
            remainingPieces.add(QuartoPiece.toQuartoPiece(b));
        }
    }

    public int[][] getWinningCoords() {
        ArrayList<Integer> rows = new ArrayList<Integer>();
        ArrayList<Integer> cols = new ArrayList<Integer>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                byte[] hazards = qb.getHazards(i, j);
                int[] counts = qb.getCounts(i, j);

                for (int k = 0; k < hazards.length; k++) {
                    if (counts[k] >= 4 && hazards[k] != 0) {
                        rows.add(i);
                        cols.add(j);
                    }
                }
            }
        }

        int[][] r = new int[2][rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            r[0][i] = rows.get(i);
            r[1][i] = cols.get(i);
        }

        return r;
    }

    @Override
    public boolean pieceAt(int row, int col) {
        return qb.pieceAt(row, col) != 0;
    }

    public boolean placePiece(int row, int col, QuartoPiece piece) {
        if (pieceAt(row, col)) return false;
        qb.move((byte)piece.hashCode(), row, col);
        remainingPieces.remove(piece);
        return true;
    }

    @Override
    public QuartoPiece getPiece(int row, int col) {
        if (!pieceAt(row, col)) return null;
        return QuartoPiece.toQuartoPiece(qb.pieceAt(row, col));
    }

    @Override
    public Set<QuartoPiece> getRemainingPieces() {
        return remainingPieces;
    }

    @Override
    public boolean won() {
        return qb.won();
    }
}