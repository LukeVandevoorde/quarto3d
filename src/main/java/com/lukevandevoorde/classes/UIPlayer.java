package com.lukevandevoorde.classes;

import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class UIPlayer extends Player {
    
    private BoardDrawable boardDrawable;
    private PieceOfferingHolder offerDropLocation;
    private PieceOfferingHolder offerReceivingLocation;
    private GameFlowManager manager;

    public UIPlayer(BoardDrawable boardDrawable, PieceOfferingHolder offerReceivingLocation, PieceOfferingHolder offerDropLocation) {
        this.boardDrawable = boardDrawable;
        this.offerReceivingLocation = offerReceivingLocation;
        this.offerDropLocation = offerDropLocation;
    }

    @Override
    public void choosePlacement(GameFlowManager manager, QuartoBoardState board, QuartoPiece pieceToPlace) {
        this.manager = manager;
        this.offerReceivingLocation.enableRemoval();
        this.boardDrawable.requestNotification(this);
    }

    public void notifyPlacement(int row, int col, QuartoPiece piecePlaced) {
        this.offerReceivingLocation.disableRemoval();
        this.manager.notifyPlacement(this, piecePlaced, row, col);
    }

    @Override
    public void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board) {
        this.manager = manager;
        this.offerDropLocation.enableDrop();
        this.offerDropLocation.requestNotification(this);
    }

    public void notifyOffering(QuartoPiece piece) {
        this.offerDropLocation.disableDrop();
        this.manager.notifyOffering(this, piece);
    }
}
