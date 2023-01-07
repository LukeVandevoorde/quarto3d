package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class UIPlayer extends Player {
    
    public static final AnimationSpeed SELECT_SPEED = new AnimationSpeed(2500, 3.5f);
    public static final AnimationSpeed USER_VIEW_SPEED = new AnimationSpeed(1700, 2.5f);
    public static final AnimationSpeed OFFERING_SPEED = new AnimationSpeed(800, 1);
    public static final AnimationSpeed DROP_SPEED = new AnimationSpeed(3000, 3.5f);

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
        Main.UI_COORDINATOR.setMaxPriority(2);
        this.manager = manager;
        this.offerReceivingLocation.enableUIRemoval();
        this.boardDrawable.requestNotification(this);
    }

    public void notifyPlacement(int row, int col, QuartoPiece piecePlaced) {
        Main.UI_COORDINATOR.setMaxPriority(1);
        this.offerReceivingLocation.disableUIRemoval();
        this.manager.notifyPlacement(this, piecePlaced, row, col);
    }

    @Override
    public void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board) {
        Main.UI_COORDINATOR.setMaxPriority(2);
        this.manager = manager;
        this.offerDropLocation.enableUIDrop();
        this.offerDropLocation.setListener(this);
    }

    public void notifyOffering(QuartoPiece piece) {
        Main.UI_COORDINATOR.setMaxPriority(1);
        this.offerDropLocation.disableUIDrop();
        this.manager.notifyOffering(this, piece);
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
