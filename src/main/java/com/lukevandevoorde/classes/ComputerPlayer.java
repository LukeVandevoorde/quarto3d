package com.lukevandevoorde.classes;

import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class ComputerPlayer extends Player {

    public ComputerPlayer() {}

    @Override
    public void choosePlacement(GameFlowManager manager, QuartoBoardState board, QuartoPiece pieceToPlace) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (!board.pieceAt(row, col)) {
                    manager.notifyPlacement(this, pieceToPlace, row, col);
                    return;
                }
            }
        }
        
    }

    @Override
    public void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board) {
        QuartoPiece offering = board.getRemainingPieces().iterator().next();
        manager.notifyOffering(this, offering);
    }
    
}
