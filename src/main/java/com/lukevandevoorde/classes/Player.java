package com.lukevandevoorde.classes;

import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public abstract class Player {

    // When done, notifies manager of placement
    public abstract void choosePlacement(GameFlowManager manager, QuartoBoardState board, QuartoPiece pieceToPlace);

    // When done, notifies manager of offering
    public abstract void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board);

}
