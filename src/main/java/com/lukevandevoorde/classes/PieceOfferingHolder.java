package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;

import processing.core.PVector;
import processing.core.PGraphics;

public class PieceOfferingHolder extends Drawable implements DragTarget<PieceDraggable> {
    
    private PieceDraggable drag;
    private DragTarget<PieceDraggable> board;
    private Draggable.CallBack dCallBack;
    private boolean occupied;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<PieceDraggable> board, Draggable.CallBack boardPlaceCallback) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.dCallBack = boardPlaceCallback;
        occupied = false;
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.push();
        if (drag != null) {
            drag.draw();
        }
        this.transform.transform(graphics);
        graphics.noFill();
        graphics.strokeWeight(5);
        graphics.rect(0, 0, dimensions.x, dimensions.y);

        graphics.pop();
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
    public boolean accept(Draggable<PieceDraggable> draggable) {
        if (occupied || !mouseHover(Main.MOUSE_COORDINATOR.getMouseX(), Main.MOUSE_COORDINATOR.getMouseY())) return false;

        occupied = true;
        PieceDraggable d = draggable.getPayload();
        this.drag = d;
        
        Draggable.CallBack removeCallBack = new Draggable.CallBack() {
            public void onAccept() {
                drag = null;
                occupied = false;
            }
        };
        
        d.addTarget(board);
        d.addCallback(this.dCallBack);
        d.addCallback(removeCallBack);
        
        return true;
    }
}
