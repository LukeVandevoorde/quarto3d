package com.lukevandevoorde.bot;

public class Move {
    public int row, col;
    public byte pieceToOffer;
    public float score;

    public Move(int row, int col, byte pieceToOffer, float score) {
        this.row = row;
        this.col = col;
        this.pieceToOffer = pieceToOffer;
        this.score = score;
    }
}
