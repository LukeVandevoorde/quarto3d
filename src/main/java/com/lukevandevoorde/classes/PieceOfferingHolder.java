package com.lukevandevoorde.classes;

import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PVector;
import processing.core.PGraphics;

public class PieceOfferingHolder extends Drawable implements DragTarget<QuartoPiece> {
    
    private QuartoPiece offering;
    private PieceDraggable drag;
    private DragTarget<QuartoPiece> board;
    private Draggable.CallBack dCallBack;
    private boolean occupied;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<QuartoPiece> board, Draggable.CallBack dCallBack) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.dCallBack = dCallBack;
        occupied = false;
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.pushMatrix();

        


        graphics.popMatrix();
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        dimensions.x = newDimensions.x;
        dimensions.y = newDimensions.y;
        dimensions.z = newDimensions.z;
    }

    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        float mx = viewport.effectiveX(mouseX);
        float my = viewport.effectiveY(mouseY);
        float x = transform.getX();
        float y = transform.getY();
        return mx >= x && my >= y && mx < x + dimensions.x && my < y + dimensions.y;
    }

    @Override
    public boolean accept(Draggable<QuartoPiece> draggable) {
        if (occupied) return false;

        this.offering = draggable.getPayload();
        this.drag = new PieceDraggable(viewport, transform, new PVector(dimensions.x/2, dimensions.y/2, dimensions.y), offering);
        this.drag.addTarget(board);
        this.drag.addCallback(this.dCallBack);

        return true;
    }
}
