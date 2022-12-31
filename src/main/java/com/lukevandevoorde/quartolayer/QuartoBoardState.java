package com.lukevandevoorde.quartolayer;

import java.util.Set;

public interface QuartoBoardState {
    
    // returns true if the board has a piece at (row, col)
    public boolean pieceAt(int row, int col);

    // returns the piece at (row, col)
    public QuartoPiece getPiece(int row, int col);

    // returns a set of all QuartoPieces not currently on the board
    public Set<QuartoPiece> getRemainingPieces();
    
}
