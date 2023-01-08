package com.lukevandevoorde.quartolayer;

import java.util.HashSet;

public class QuartoPiece {

    private boolean tall, light, square, filled;

    public QuartoPiece(boolean tall, boolean light, boolean square, boolean filled) {
        this.tall = tall;
        this.light = light;
        this.square = square;
        this.filled = filled;
    }

    public boolean getTall() { return tall; }
    
    public boolean getLight() { return light; }

    public boolean getSquare() { return square; }

    public boolean getFilled() { return filled; }

    public static HashSet<QuartoPiece> allPieces() {
        HashSet<QuartoPiece> pieces = new HashSet<QuartoPiece>();
        for (int i = 0; i < 16; i++) {
            pieces.add(toQuartoPiece(quartoPiece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0)));
        }
        return pieces;
    }

    public static HashSet<Byte> allBytes() {
        HashSet<Byte> pieces = new HashSet<Byte>(16);
        for (int i = 0; i < 16; i++) {
            pieces.add(quartoPiece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0));
        }
        return pieces;
    }

    public static QuartoPiece toQuartoPiece(int hashCode) {
        return new QuartoPiece((hashCode & 1) != 0, (hashCode & 4) != 0, (hashCode & 16) != 0, (hashCode & 64) != 0);
    }

    public static byte quartoPiece(QuartoPiece p) {
        return (byte) (p.hashCode());
    }

    public static byte quartoPiece(boolean isTall, boolean isLight, boolean isSquare, boolean isFilled) {
        return (byte) (new QuartoPiece(isTall, isLight, isSquare, isFilled).hashCode());
    }

    public static int pieceToID(QuartoPiece piece) {
        return pieceToID(quartoPiece(piece));
        
    }

    public static int pieceToID(byte piece) {
        return (piece & 1) | ((piece & 4) >> 1) | ((piece & 16) >> 2) | ((piece & 64) >> 3);
    }

    public byte asByte() {
        byte p = 0;
        p |= tall ? 1 : 2;
        p |= light ? 4 : 8;
        p |= square ? 16 : 32;
        p |= filled ? 64 : 128;
        return p;
    }

    @Override
    public int hashCode() {
        byte p = 0;
        p |= tall ? 1 : 2;
        p |= light ? 4 : 8;
        p |= square ? 16 : 32;
        p |= filled ? 64 : 128;
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof QuartoPiece)) return false;
        QuartoPiece other = (QuartoPiece) o; 
        return this.tall == other.tall && this.light == other.light && this.square == other.square && this.filled == other.filled;
    }
}
