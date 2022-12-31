package com.lukevandevoorde.classes;

import com.lukevandevoorde.Board;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class GameFlowManager {

    private enum TurnState {
        P1_PLACING,
        P1_OFFERING,
        P2_PLACING,
        P2_OFFERING
    }

    public static final int P1 = 0;
    public static final int P2 = 1;

    private Board quartoBoard;
    private TurnState turnState;
    private QuartoPiece offering;
    private Player[] players;

    public GameFlowManager() {
        this.quartoBoard = new Board();
        this.turnState = TurnState.P1_OFFERING;
        this.offering = null;
        this.players = new Player[2];
    }

    public void registerPlayer(Player player, int side) {
        this.players[side] = player;
    }

    public void startGame() {
        players[P1].selectPieceToOffer(this, quartoBoard);
    }

    public QuartoBoardState getQuartoBoardState() {
        return this.quartoBoard;
    }

    public void notifyPlacement(Player player, int row, int col, QuartoPiece quartoPiece) {
        if (turnState == TurnState.P1_OFFERING || turnState == TurnState.P2_OFFERING) throw new IllegalStateException("Tried to place during offer phase");
        if (quartoPiece != offering) throw new IllegalStateException("Tried to place the wrong piece");

        quartoBoard.placePiece(row, col, quartoPiece);
        // TODO: check win
        if (turnState == TurnState.P1_PLACING) {
            turnState = TurnState.P1_OFFERING;
            players[P1].selectPieceToOffer(this, quartoBoard);
        } else if (turnState == TurnState.P2_PLACING) {
            turnState = TurnState.P2_OFFERING;
            players[P2].selectPieceToOffer(this, quartoBoard);
        }
    }

    public void notifyOffering(Player player, QuartoPiece offering) {
        if (turnState == TurnState.P1_PLACING || turnState == TurnState.P2_PLACING) throw new IllegalStateException("Tried to offer during placement phase");

        if (turnState == TurnState.P1_OFFERING) {
            turnState = TurnState.P2_PLACING;
            this.offering = offering;
            players[P2].choosePlacement(this, quartoBoard, offering);
        } else if (turnState == TurnState.P2_OFFERING) {
            turnState = TurnState.P1_PLACING;
            this.offering = offering;
            players[P1].choosePlacement(this, quartoBoard, offering);
        }
    }
}
