package com.lukevandevoorde.bot;

import com.lukevandevoorde.quartolayer.QuartoPiece;

public class Move {
    public int score, row, col;
    public byte pieceToOffer;

    public Move(int score, int row, int col, byte pieceToOffer) {
        this.score = score;
        this.row = row;
        this.col = col;
        this.pieceToOffer = pieceToOffer;
    }

    @Override
    public String toString() {
        QuartoPiece p = QuartoPiece.toQuartoPiece(pieceToOffer);
        String res =  "(" + row + ", " + col + "), Score: " + score + ", offer ";
        res += p.getTall() ? "Tall, " : "Short, ";
        res += p.getLight() ? "Light, " : "Dark, ";
        res += p.getSquare() ? "Square, " : "Circle, ";
        res += p.getFilled() ? "Filled" : "Hollow";
        return res;
    }
}
