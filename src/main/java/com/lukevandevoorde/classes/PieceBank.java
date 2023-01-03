package com.lukevandevoorde.classes;

import java.util.Set;

import java.util.ArrayList;
import java.util.Collection;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceBank extends Drawable {
    private static final float PIECE_PROPORTION = 0.35f;
    private static final float PADDING_PROP = 0.025f;

    private static final float BASE_TILT = -(0.33f*PApplet.PI);

    private ArrayList<PieceDraggable> drags;
    private float baseWidth;

    private PVector paddedPosition, paddedDimensions;
    private float rectRadius;

    public PieceBank(Viewport viewport, TransformData transform, PVector dimensions, Set<QuartoPiece> pieces,
                        Collection<DragTarget<PieceDraggable>> targets) {
        super(viewport, transform, dimensions);
        
        baseWidth = 5; // random garbage, will be set later
        this.drags = new ArrayList<PieceDraggable>();

        for (QuartoPiece p: pieces) {
            PieceDraggable draggable = new PieceDraggable(viewport, new TransformData(), new PVector(), p);
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

        paddedPosition = new PVector();
        paddedDimensions = new PVector();
        setDimensions(dimensions);
        setTransform(transform);
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        this.dimensions.set(newDimensions);
        float padding = PADDING_PROP * Math.min(dimensions.x, dimensions.y);
        this.paddedPosition.x = padding;
        this.paddedPosition.y = padding;
        this.paddedDimensions.x = dimensions.x - 2*padding;
        this.paddedDimensions.y = dimensions.y - 2*padding;
        this.baseWidth = Math.min(paddedDimensions.x/2, paddedDimensions.y/4) * PIECE_PROPORTION;
        this.rectRadius = Math.min(paddedDimensions.x, paddedDimensions.y) * PADDING_PROP;
        for (PieceDraggable piece: drags) {
            piece.setDimensions(new PVector(baseWidth, baseWidth, PieceDrawable.HEIGHT_TO_WIDTH_RATIO * baseWidth));
        }
    }

    @Override
    public void setTransform(TransformData newTransform) {
        this.transform.set(newTransform);
        for (PieceDraggable piece: drags) {
            piece.setTransform(new TransformData(pieceMapper(piece.getPiece()).add(transform.getPosition()).add(paddedPosition), new PVector(BASE_TILT,0,0)));
            piece.returnToBase(0);
        }
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.push();
        transform.transform(graphics);
        graphics.noFill();
        graphics.strokeWeight(5);
        graphics.rect(paddedPosition.x, paddedPosition.y, paddedDimensions.x, paddedDimensions.y, rectRadius);
        graphics.pop();

        for (PieceDraggable piece: drags) {
            piece.draw();
        }
    }

    private PVector pieceMapper(QuartoPiece piece) {
        PVector pos = new PVector();
        pos.x += paddedDimensions.x * 0.25f;
        
        if (piece.getLight()) {
            pos.x += paddedDimensions.x * 0.5f;
        }

        float padding = 0.1f;
        float total = 2*(1 + PieceDrawable.SHORT_TO_TALL_HEIGHT_RATIO) + 5*padding;
        float tallProp = 1/total;
        float shortProp = PieceDrawable.SHORT_TO_TALL_HEIGHT_RATIO/total;
        float paddingProp = padding/total;

        pos.y += paddedDimensions.y * (0.5f*tallProp);

        if (piece.getTall()) {
            if (!piece.getSquare()) {
                pos.y += paddedDimensions.y * (paddingProp + tallProp);
            }
        } else {
            pos.y += paddedDimensions.y * (1.5f*tallProp + 0.5f*shortProp + 2*paddingProp);
            if (!piece.getSquare()) {
                pos.y += paddedDimensions.y * (paddingProp + shortProp);
            }
        }

        return pos;
    }
}
