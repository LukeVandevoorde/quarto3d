package com.lukevandevoorde.classes;

import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class ComputerPlayer extends Player {

    public static final AnimationSpeed SELECT_SPEED = new AnimationSpeed(1000, 2);
    public static final AnimationSpeed USER_VIEW_SPEED = new AnimationSpeed(1200, 2);
    public static final AnimationSpeed OFFERING_SPEED = new AnimationSpeed(1000, 1);
    public static final AnimationSpeed DROP_SPEED = new AnimationSpeed(1000, 1);

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

    @Override
    public AnimationSpeed selectViewSpeed() {
        return SELECT_SPEED;
    }

    @Override
    public AnimationSpeed userViewSpeed() {
        return USER_VIEW_SPEED;
    }

    @Override
    public AnimationSpeed pieceOfferingSpeed() {
        return OFFERING_SPEED;
    }

    @Override
    public AnimationSpeed dropSpeed() {
        return DROP_SPEED;
    }
}
