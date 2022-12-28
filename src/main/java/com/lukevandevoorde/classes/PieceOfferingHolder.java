package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;

import processing.core.PGraphics;
import processing.core.PVector;

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
    public boolean willAccept(Draggable<PieceDraggable> draggable) {
        return !occupied && mouseHover(Main.MOUSE_COORDINATOR.getMouseX(), Main.MOUSE_COORDINATOR.getMouseY());
    }

    @Override
    public boolean accept(Draggable<PieceDraggable> draggable) {
        if (!willAccept(draggable)) return false;

        occupied = true;
        this.drag = draggable.getPayload();
        this.drag.setTransform(new TransformData(this.transform.getPosition().add(dimensions.x/2, dimensions.y/2), this.drag.getBaseTransform().getRotation()));

        Draggable.CallBack removeCallBack = new Draggable.CallBack() {
            public void onAccept() {
                drag = null;
                occupied = false;
            }
        };
        
        Main.MOUSE_COORDINATOR.add(this.drag);
        this.drag.addTarget(board);
        this.drag.addCallback(dCallBack);
        this.drag.addCallback(removeCallBack);
        
        return true;
    }
}
