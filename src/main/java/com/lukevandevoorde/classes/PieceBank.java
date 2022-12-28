package com.lukevandevoorde.classes;

import java.util.Set;

import java.util.ArrayList;
import java.util.Collection;

import processing.core.PApplet;
import processing.core.PVector;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceBank extends Drawable {
    private static final float PIECE_PROPORTION = 0.4f;

    private ArrayList<PieceDraggable> drags;
    private float baseWidth;

    public PieceBank(Viewport viewport, TransformData transform, PVector dimensions, Set<QuartoPiece> pieces,
                        Collection<DragTarget<PieceDraggable>> targets) {
        super(viewport, transform, dimensions);
        this.baseWidth = Math.min(viewport.width()/2, viewport.height()/4) * PIECE_PROPORTION;

        this.drags = new ArrayList<PieceDraggable>();

        for (QuartoPiece p: pieces) {
            PieceDraggable draggable = new PieceDraggable(viewport,
                                            new TransformData(pieceMapper(p).add(transform.getPosition()), new PVector(-(0.35f*PApplet.PI),0,0)),
                                            new PVector(baseWidth, baseWidth, baseWidth * PieceDrawable.HEIGHT_TO_WIDTH_RATIO),
                                            p);
            draggable.addCallback(
                new Draggable.CallBack() {
                    public void onAccept() {
                        drags.remove(draggable);
                    }
                }
            );
            targets.forEach(target -> draggable.addTarget(target));
            drags.add(draggable);
        }
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        this.baseWidth = Math.min(dimensions.x/2, dimensions.y/4) * PIECE_PROPORTION;
        for (PieceDraggable piece: drags) {
            piece.setDimensions(new PVector(baseWidth, baseWidth, PieceDrawable.HEIGHT_TO_WIDTH_RATIO * baseWidth));
        }
    }

    @Override
    public void draw() {
        viewport.getGraphics().pushMatrix();
        for (PieceDraggable piece: drags) {
            piece.draw();
        }
        viewport.getGraphics().popMatrix();
    }

    private PVector pieceMapper(QuartoPiece piece) {
        PVector pos = new PVector();
        pos.x += dimensions.x / 4;
        pos.y += dimensions.y / 8;

        if (piece.getLight()) {
            pos.x += dimensions.x/2;
        }

        if (!piece.getSquare()) {
            pos.y += dimensions.y/4;
        }

        if (!piece.getTall()) {
            pos.y += dimensions.y/2;
        }

        return pos;
    }
}
