package com.lukevandevoorde.bot;

public class Move {
    public int row, col;
    public byte pieceToOffer;

    public Move(int row, int col, byte pieceToOffer) {
        this.row = row;
        this.col = col;
        this.pieceToOffer = pieceToOffer;
    }
}
