package com.lukevandevoorde;

public class Piece implements QuartoPiece {

    private boolean tall, light, square, filled;

    public Piece(boolean tall, boolean light, boolean square, boolean filled) {
        this.tall = tall;
        this.light = light;
        this.square = square;
        this.filled = filled;
    }

    @Override
    public boolean getTall() { return tall; }
    
    @Override
    public boolean getLight() { return light; }

    @Override
    public boolean getSquare() { return square; }

    @Override
    public boolean getFilled() { return filled; }
}