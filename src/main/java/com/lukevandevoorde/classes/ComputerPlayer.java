package com.lukevandevoorde.classes;

import com.lukevandevoorde.bot.Bot;
import com.lukevandevoorde.bot.Move;
import com.lukevandevoorde.bot.QB;
import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class ComputerPlayer extends Player {

    public static final AnimationSpeed SELECT_SPEED = new AnimationSpeed(1000, 2);
    public static final AnimationSpeed USER_VIEW_SPEED = new AnimationSpeed(1200, 2);
    public static final AnimationSpeed OFFERING_SPEED = new AnimationSpeed(1000, 1);
    public static final AnimationSpeed DROP_SPEED = new AnimationSpeed(1000, 1);

    private Move move;

    public ComputerPlayer() {}

    @Override
    public void choosePlacement(GameFlowManager manager, QuartoBoardState board, QuartoPiece pieceToPlace) {

        Bot bot = new Bot(3);
        QB qb = new QB();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (board.getPiece(row, col) != null) {
                    qb.move(board.getPiece(row, col).asByte(), row, col);
                }
            }
        }

        if (qb.remainingPieces().size() != board.getRemainingPieces().size()) throw new IllegalStateException("gahh");

        ComputerPlayer me = this;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                move = bot.nextMove(qb, QuartoPiece.quartoPiece(pieceToPlace));
                System.out.println("Move: " + move);
                System.out.println();
                manager.notifyPlacement(me, pieceToPlace, move.row, move.col);
            }
        });

        thread.start();
    }

    @Override
    public void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board) {
        manager.notifyOffering(this, QuartoPiece.toQuartoPiece(move.pieceToOffer));
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
