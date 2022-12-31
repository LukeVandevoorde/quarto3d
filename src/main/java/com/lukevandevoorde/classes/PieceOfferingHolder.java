package com.lukevandevoorde.classes;

import java.util.function.Function;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PGraphics;
import processing.core.PVector;

public class PieceOfferingHolder extends Drawable implements DragTarget<PieceDraggable> {
    
    private PieceDraggable drag;
    private DragTarget<PieceDraggable> board;
    private Draggable.CallBack boardPlaceCallback;
    private boolean occupied;

    private boolean dropEnabled, removalEnabled;
    private UIPlayer notify;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<PieceDraggable> board, Draggable.CallBack boardPlaceCallback) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.boardPlaceCallback = boardPlaceCallback;
        this.occupied = false;
        this.dropEnabled = false;
        this.removalEnabled = false;
    }

    public QuartoPiece getQuartoPiece() {
        if (!occupied) return null;

        return this.drag.getPiece();
    }

    // Will notify player when a piece is dropped into this holder
    public void requestNotification(UIPlayer player) {
        this.notify = player;
    }

    public void enableDrop() {
        this.dropEnabled = true;
    }

    public void disableDrop() {
        this.dropEnabled = false;
    }

    public void enableRemoval() {
        this.removalEnabled = true;
        if (this.drag != null) Main.MOUSE_COORDINATOR.add(drag);
    }

    public void disableRemoval() {
        this.removalEnabled = false;
        if (this.drag != null) Main.MOUSE_COORDINATOR.remove(drag);
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
        return !occupied && dropEnabled && mouseHover(Main.MOUSE_COORDINATOR.getMouseX(), Main.MOUSE_COORDINATOR.getMouseY());
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
        
        if (removalEnabled) Main.MOUSE_COORDINATOR.add(this.drag);
        this.drag.addTarget(board);
        this.drag.addCallback(boardPlaceCallback);
        this.drag.addCallback(removeCallBack);
        
        if (this.notify != null) notify.notifyOffering(this.drag.getPiece());

        return true;
    }
}
