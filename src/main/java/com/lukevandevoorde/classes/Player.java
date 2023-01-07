package com.lukevandevoorde.classes;

import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public abstract class Player {

    public static final int P1_COLOR = 0xFF33DD33;
    public static final int P2_COLOR = 0xFF3333DD;

    // When done, notifies manager of placement
    public abstract void choosePlacement(GameFlowManager manager, QuartoBoardState board, QuartoPiece pieceToPlace);

    // When done, notifies manager of offering
    public abstract void selectPieceToOffer(GameFlowManager manager, QuartoBoardState board);

    public abstract AnimationSpeed selectViewSpeed();

    public abstract AnimationSpeed userViewSpeed();

    public abstract AnimationSpeed pieceOfferingSpeed();

    public abstract AnimationSpeed dropSpeed();

}
