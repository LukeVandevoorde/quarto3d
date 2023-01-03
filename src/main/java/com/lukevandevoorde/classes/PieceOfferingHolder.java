package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class PieceOfferingHolder extends Drawable implements DragTarget<PieceDraggable> {

    private class Indicator extends Drawable {
        public Indicator(Viewport viewport, TransformData transform, PVector dimensions) {
            super(viewport, transform, dimensions);
        }

        @Override
        public void draw() {
            PGraphics graphics = viewport.getGraphics();
            graphics.push();
            transform.transform(graphics);
            graphics.strokeWeight(3);
            graphics.fill(graphics.color(0));
            graphics.beginShape(PGraphics.TRIANGLE);
            graphics.vertex(dimensions.x/2, 0, 0);
            graphics.vertex(-dimensions.x/2, dimensions.y, 0);
            graphics.vertex(-dimensions.x/2, -dimensions.y, 0);
            graphics.endShape();
            graphics.pop();
        }

        @Override
        public void setDimensions(PVector newDimensions) {
            this.dimensions.set(newDimensions);
        }
    }
    
    private static final float PADDING_PROP = 0.025f;

    private PieceDraggable drag;
    private DragTarget<PieceDraggable> board;
    private Draggable.CallBack boardPlaceCallback;
    private boolean occupied;

    private boolean dropEnabled, removalEnabled;
    private UIPlayer notify;

    private PVector paddedPosition, paddedDimensions;
    private AnimatedDrawable indicator;
    private TransformData dropTransform, removeTransform;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<PieceDraggable> board, Draggable.CallBack boardPlaceCallback, boolean indicatorOnRight) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.boardPlaceCallback = boardPlaceCallback;
        this.occupied = false;
        this.dropEnabled = false;
        this.removalEnabled = false;

        this.paddedPosition = new PVector(dimensions.x * PADDING_PROP, dimensions.y * PADDING_PROP);
        this.paddedDimensions = new PVector(dimensions.x * (1 - 2*PADDING_PROP), dimensions.y * (1 - 2*PADDING_PROP), PADDING_PROP*Math.min(dimensions.x, dimensions.y));

        float len = Math.min(dimensions.x, dimensions.y)/6;
        PVector dim = new PVector(len, len/3.3f);

        if (indicatorOnRight) {
            dropTransform = new TransformData(new PVector(dim.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI));
            removeTransform = new TransformData(new PVector(dim.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.2f));
        } else {
            dropTransform = new TransformData(new PVector(-dim.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, 0));
            removeTransform = new TransformData(new PVector(-dim.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.8f));
        }

        indicator = new AnimatedDrawable(new Indicator(viewport, dropTransform, dim));
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
        this.indicator.animate(dropTransform, null, 0);
    }

    public void disableDrop() {
        this.dropEnabled = false;
    }

    public void enableRemoval() {
        this.removalEnabled = true;
        if (this.drag != null) Main.MOUSE_COORDINATOR.add(drag);
        this.indicator.animate(removeTransform, null, 400);
    }

    public void disableRemoval() {
        this.removalEnabled = false;
        if (this.drag != null) Main.MOUSE_COORDINATOR.remove(drag);
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.push();
        if (drag != null) drag.draw();

        this.transform.transform(graphics);
        if (dropEnabled || removalEnabled) indicator.draw();

        graphics.noFill();
        graphics.strokeWeight(5);
        graphics.rect(paddedPosition.x, paddedPosition.y, paddedDimensions.x, paddedDimensions.y, paddedDimensions.z);

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
        float newWidth = Math.min(dimensions.x * 0.6f, dimensions.y * 0.6f / PieceDrawable.HEIGHT_TO_WIDTH_RATIO);
        drag.setDimensions(new PVector(newWidth, newWidth, newWidth*PieceDrawable.HEIGHT_TO_WIDTH_RATIO));

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
