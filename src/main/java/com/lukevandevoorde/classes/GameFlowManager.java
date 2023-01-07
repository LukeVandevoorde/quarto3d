package com.lukevandevoorde.classes;

import java.lang.reflect.Field;
import java.util.Set;

import com.lukevandevoorde.Board;
import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.TimeKeeper;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class GameFlowManager {

    private enum TurnState {
        P1_PLACING,
        P1_OFFERING,
        P2_PLACING,
        P2_OFFERING,
        P1_WON,
        P2_WON
    }

    @SuppressWarnings("unused")
    private static final String P1_PLACING = "Player 1: Place the piece";
    @SuppressWarnings("unused")
    private static final String P1_OFFERING = "Player 1: Offer a piece";
    @SuppressWarnings("unused")
    private static final String P2_PLACING = "Player 2: Place the piece";
    @SuppressWarnings("unused")
    private static final String P2_OFFERING = "Player 2: Offer a piece";
    @SuppressWarnings("unused")
    private static final String P1_WON = "Player 1 Wins";
    @SuppressWarnings("unused")
    private static final String P2_WON = "Player 2 Wins";

    public static final int P1 = 0;
    public static final int P2 = 1;

    private Board quartoBoard;
    private TurnState turnState;
    private QuartoPiece offering;
    private Player[] players;
    private BoardDrawable boardDrawable;
    private PieceOfferingHolder[] holders;
    private Set<PieceBank> pieceBanks;

    public GameFlowManager() {
        this.quartoBoard = new Board();
        this.turnState = TurnState.P1_OFFERING;
        this.offering = null;
        this.players = new Player[2];
        this.holders = new PieceOfferingHolder[2];
    }

    public String getUIHint() {
        try {
            Field hint = GameFlowManager.class.getDeclaredField(turnState.toString());
            return (String) hint.get(null);
        } catch (NoSuchFieldException e) {
            return "No hint available";
        } catch (IllegalAccessException e) {
            return "Something went wrong";
        }
    }

    public void registerPlayer(Player player, int side) {
        this.players[side] = player;
    }

    public void registerBoardDrawable(BoardDrawable boardDrawable) {
        this.boardDrawable = boardDrawable;
    }

    public void registerPieceOfferingHolder(PieceOfferingHolder holder, int side) {
        this.holders[side] = holder;
    }

    public void registerPieceBanks(Set<PieceBank> banks) {
        this.pieceBanks = banks;
    }

    public void startGame() {
        holders[P2].indicateDrop();
        players[P1].selectPieceToOffer(this, quartoBoard);
    }

    public QuartoBoardState getQuartoBoardState() {
        return this.quartoBoard;
    }

    public void notifyPlacement(Player player, QuartoPiece quartoPiece, int row, int col) {
        if (turnState != TurnState.P1_PLACING && turnState != TurnState.P2_PLACING) throw new IllegalStateException("Tried to place during offer phase or win state");
        if (turnState == TurnState.P1_PLACING && player == players[P2] || turnState == TurnState.P2_PLACING && player == players[P1]) throw new IllegalStateException("Players played out of turn");
        if (quartoPiece != offering) throw new IllegalStateException("Tried to place the wrong piece");

        quartoBoard.placePiece(row, col, quartoPiece);

        TurnState selectState, winState;
        PieceOfferingHolder sourceHolder, destinationHolder;
        int winColor;

        if (turnState == TurnState.P1_PLACING) {
            selectState = TurnState.P1_OFFERING;
            winState = TurnState.P1_WON;
            sourceHolder = holders[P1];
            destinationHolder = holders[P2];
            winColor = Player.P1_COLOR;
        } else {
            selectState = TurnState.P2_OFFERING;
            winState = TurnState.P2_WON;
            sourceHolder = holders[P2];
            destinationHolder = holders[P1];
            winColor = Player.P2_COLOR;
        }

        int timeToPlacementAnimation = boardDrawable.enterSelectView(player.selectViewSpeed());

        GameFlowManager manager = this;
        TimeKeeper.Job delayPlacementAnimation = new TimeKeeper.Job() {
            public void execute() {
                PieceDraggable draggable = sourceHolder.getPieceDraggable();
                sourceHolder.remove();
                sourceHolder.hideRemovalIndicator(boardDrawable.handOff(draggable, row, col, player.dropSpeed()));
                int timeToView = boardDrawable.enterUserView(player.userViewSpeed());
                
                if (quartoBoard.won()) {
                    turnState = winState;
                    if (boardDrawable != null) boardDrawable.highlightWin(quartoBoard.getWinningCoords(), winColor);
                } else {
                    turnState = selectState;

                    TimeKeeper.Job delaySelection = new TimeKeeper.Job() {
                        public void execute() {
                            destinationHolder.indicateDrop();
                            player.selectPieceToOffer(manager, quartoBoard);
                        }
                    };

                    Main.TIME_KEEPER.scheduleJob(timeToView, delaySelection);
                }
            }
        };
        
        Main.TIME_KEEPER.scheduleJob(timeToPlacementAnimation, delayPlacementAnimation);
    }

    public void notifyOffering(Player player, QuartoPiece offering) {
        if (turnState != TurnState.P1_OFFERING && turnState != TurnState.P2_OFFERING) throw new IllegalStateException("Tried to offer during placement phase or win state");
        if (turnState == TurnState.P1_OFFERING && player == players[P2] || turnState == TurnState.P2_OFFERING && player == players[P1]) throw new IllegalStateException("Players offered out of turn");

        TurnState placeState;
        Player otherPlayer;
        PieceOfferingHolder holder;

        if (turnState == TurnState.P1_OFFERING) {
            placeState = TurnState.P2_PLACING;
            otherPlayer = players[P2];
            holder = holders[P2];
        } else {
            placeState = TurnState.P1_PLACING;
            otherPlayer = players[P1];
            holder = holders[P1];
        }

        turnState = placeState;
        this.offering = offering;

        PieceDraggable drag = null;
        PieceBank bank = null;
        for (PieceBank pb: this.pieceBanks) {
            bank = pb;
            drag = pb.match(offering);
            if (drag != null) break;
        }

        if (drag == null) throw new IllegalStateException("Tried to offer a piece that doesn't exist");
        bank.withdraw(drag);
        int timeToPlacement = holder.handOff(drag, player.pieceOfferingSpeed());

        GameFlowManager manager = this;
        TimeKeeper.Job delayPlacement = new TimeKeeper.Job() {
            public void execute() {
                holder.switchIndication();
                otherPlayer.choosePlacement(manager, quartoBoard, offering);
            }
        };

        Main.TIME_KEEPER.scheduleJob(timeToPlacement, delayPlacement);
    }
}
