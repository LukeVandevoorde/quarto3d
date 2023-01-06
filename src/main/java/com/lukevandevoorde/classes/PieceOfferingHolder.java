package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class PieceOfferingHolder extends Drawable implements DragTarget<QuartoPiece> {

    private static final int HANDOFF_MILLIS = 350;

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
    private DragTarget<QuartoPiece> board;
    private Draggable.CallBack boardPlaceCallback;

    private boolean uiDropEnabled, uiRemovalEnabled;
    private UIPlayer notify;

    private PVector paddedPosition, paddedDimensions;
    private AnimatedDrawable indicator;
    private TransformData dropTransform, removeTransform;
    private PVector labelPosition;
    private String label;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<QuartoPiece> board, Draggable.CallBack boardPlaceCallback, boolean indicatorOnRight, String label) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.boardPlaceCallback = boardPlaceCallback;
        this.uiDropEnabled = false;
        this.uiRemovalEnabled = false;
        this.label = label;

        this.paddedPosition = new PVector(dimensions.x * PADDING_PROP, dimensions.y * PADDING_PROP);
        this.paddedDimensions = new PVector(dimensions.x * (1 - 2*PADDING_PROP), dimensions.y * (1 - 2*PADDING_PROP), PADDING_PROP*Math.min(dimensions.x, dimensions.y));

        float len = Math.min(dimensions.x, dimensions.y)/6;
        PVector dim = new PVector(len, len/3.3f);

        viewport.getGraphics().push();
        if (indicatorOnRight) {
            dropTransform = new TransformData(new PVector(dim.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI));
            removeTransform = new TransformData(new PVector(dim.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.2f));
            labelPosition = new PVector(dimensions.x, paddedPosition.y + paddedDimensions.y, dimensions.y/10);
        } else {
            dropTransform = new TransformData(new PVector(-dim.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, 0));
            removeTransform = new TransformData(new PVector(-dim.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.8f));
            viewport.getGraphics().textSize(dimensions.y/10);
            labelPosition = new PVector(-viewport.getGraphics().textWidth(label), paddedPosition.y + paddedDimensions.y, dimensions.y/10);
        }
        viewport.getGraphics().pop();

        indicator = new AnimatedDrawable(new Indicator(viewport, dropTransform, dim));
    }

    public PieceDraggable getPieceDraggable() {
        return this.drag;
    }

    public void remove() {
        this.drag = null;
    }

    // Will notify player when a piece is dropped into this holder
    public void requestNotification(UIPlayer player) {
        this.notify = player;
    }

    public void enableDrop() {
        this.uiDropEnabled = true;
        this.indicator.animate(dropTransform, null, 0);
    }

    public void disableDrop() {
        this.uiDropEnabled = false;
    }

    public void enableRemoval() {
        this.uiRemovalEnabled = true;
        if (this.drag != null) Main.UI_COORDINATOR.add(drag);
        this.indicator.animate(removeTransform, null, 350);
    }

    public void disableRemoval() {
        this.uiRemovalEnabled = false;
        if (this.drag != null) Main.UI_COORDINATOR.remove(drag);
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.push();
        if (drag != null) drag.draw();

        this.transform.transform(graphics);
        if (uiDropEnabled || uiRemovalEnabled) indicator.draw();

        graphics.noFill();
        graphics.strokeWeight(5);
        graphics.rect(paddedPosition.x, paddedPosition.y, paddedDimensions.x, paddedDimensions.y, paddedDimensions.z);
        graphics.textSize(labelPosition.z);
        graphics.text(label, labelPosition.x, labelPosition.y);

        graphics.pop();
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        dimensions.x = newDimensions.x;
        dimensions.y = newDimensions.y;
        dimensions.z = newDimensions.z;
    }

    @Override
    public boolean mouseOver() {
        float mx = viewport.effectiveX(Main.UI_COORDINATOR.getMouseX());
        float my = viewport.effectiveY(Main.UI_COORDINATOR.getMouseY());
        float x = transform.getX();
        float y = transform.getY();
        return mx >= x && my >= y && mx < x + dimensions.x && my < y + dimensions.y;
    }

    @Override
    public boolean willAccept(Draggable<QuartoPiece> draggable) {
        return this.drag == null && uiDropEnabled && mouseOver();
    }

    @Override
    public boolean accept(Draggable<QuartoPiece> draggable) {
        if (!willAccept(draggable)) return false;
        if (this.notify != null) notify.notifyOffering(draggable.getPayload());
        return true;
    }

    public int handOff(PieceDraggable draggable) {
        if (this.drag != null) throw new IllegalStateException("Tried to hand offer to an occupied PieceBank");

        this.drag = draggable;
        this.drag.setTransform(new TransformData(this.transform.getPosition().add(dimensions.x/2, dimensions.y/2), this.drag.getBaseTransform().getRotation()));
        float newWidth = Math.min(dimensions.x * 0.6f, dimensions.y * 0.6f / PieceDrawable.HEIGHT_TO_WIDTH_RATIO);
        this.drag.setDimensions(new PVector(newWidth, newWidth, newWidth*PieceDrawable.HEIGHT_TO_WIDTH_RATIO));
        this.drag.addTarget(board);
        this.drag.addCallback(boardPlaceCallback);
        this.drag.returnToBase(HANDOFF_MILLIS);
        return HANDOFF_MILLIS;
    }
}
