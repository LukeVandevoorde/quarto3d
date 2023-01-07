package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class PieceOfferingHolder extends Drawable implements DragTarget<QuartoPiece> {

    private class Indicator extends Drawable {
        private int color;

        public Indicator(Viewport viewport, TransformData transform, PVector dimensions, int color) {
            super(viewport, transform, dimensions);
            this.color = color;
        }

        @Override
        public void draw() {
            PGraphics graphics = viewport.getGraphics();
            graphics.pushMatrix();
            graphics.pushStyle();
            transform.transform(graphics);
            graphics.fill(color);
            graphics.stroke(color);
            graphics.strokeWeight(3);
            graphics.beginShape(PGraphics.TRIANGLE);
            graphics.vertex(dimensions.x/2, 0, 0);
            graphics.vertex(-dimensions.x/2, dimensions.y, 0);
            graphics.vertex(-dimensions.x/2, -dimensions.y, 0);
            graphics.endShape();
            graphics.popMatrix();
            graphics.popStyle();
        }

        @Override
        public void setDimensions(PVector newDimensions) {
            this.dimensions.set(newDimensions);
        }
    }
    
    private static final float PADDING_PROP = 0.025f;
    private static final PVector ZERO_SIZE = new PVector();
    private static final int FADE_MILLIS = 165;

    private PieceDraggable drag;
    private DragTarget<QuartoPiece> board;
    private Draggable.CallBack boardPlaceCallback;

    private boolean uiDropEnabled;
    private UIPlayer notify;

    private int ownerColor;
    private PVector paddedPosition, paddedDimensions;
    private AnimatedDrawable dropIndicator, removeIndicator;
    private TransformData dropTransform, removeTransform;
    private PVector indicatorDimensions;
    private PVector labelPosition;
    private String label;

    public PieceOfferingHolder(Viewport viewport, TransformData transform, PVector dimensions, DragTarget<QuartoPiece> board, Draggable.CallBack boardPlaceCallback, int ownerColor, int dropperColor, boolean indicatorOnRight, String label) {
        super(viewport, transform, dimensions);
        this.board = board;
        this.boardPlaceCallback = boardPlaceCallback;
        this.uiDropEnabled = false;
        this.ownerColor = ownerColor;
        this.label = label;
        this.paddedPosition = new PVector(dimensions.x * PADDING_PROP, dimensions.y * PADDING_PROP);
        this.paddedDimensions = new PVector(dimensions.x * (1 - 2*PADDING_PROP), dimensions.y * (1 - 2*PADDING_PROP), PADDING_PROP*Math.min(dimensions.x, dimensions.y));

        float len = Math.min(dimensions.x, dimensions.y)/6;
        indicatorDimensions = new PVector(len, len/3.3f);

        viewport.getGraphics().push();
        if (indicatorOnRight) {
            dropTransform = new TransformData(new PVector(indicatorDimensions.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI));
            removeTransform = new TransformData(new PVector(indicatorDimensions.x/2 + 1.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.2f));
            labelPosition = new PVector(dimensions.x, paddedPosition.y + paddedDimensions.y, dimensions.y/6);
        } else {
            dropTransform = new TransformData(new PVector(-indicatorDimensions.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, 0));
            removeTransform = new TransformData(new PVector(-indicatorDimensions.x/2 - 0.1f * dimensions.x, 0.5f * dimensions.y), new PVector(0, 0, PConstants.PI * 0.8f));
            viewport.getGraphics().textSize(dimensions.y/6);
            labelPosition = new PVector(-viewport.getGraphics().textWidth(label), paddedPosition.y + paddedDimensions.y, dimensions.y/6);
        }
        viewport.getGraphics().pop();

        dropIndicator = new AnimatedDrawable(new Indicator(viewport, dropTransform, new PVector(), dropperColor));
        removeIndicator = new AnimatedDrawable(new Indicator(viewport, removeTransform, new PVector(), ownerColor));
    }

    public PieceDraggable getPieceDraggable() {
        return this.drag;
    }

    public void remove() {
        this.drag = null;
    }

    public void indicateDrop() {
        dropIndicator.setTransform(dropTransform);
        dropIndicator.setDimensions(ZERO_SIZE);
        dropIndicator.animate(dropTransform, indicatorDimensions, FADE_MILLIS);
    }

    public void switchIndication() {
        dropIndicator.animate(dropTransform, ZERO_SIZE, FADE_MILLIS);
        removeIndicator.animate(dropTransform, ZERO_SIZE, FADE_MILLIS);
        removeIndicator.animate(dropTransform, indicatorDimensions, FADE_MILLIS);
        removeIndicator.animate(removeTransform, indicatorDimensions, 325);
    }

    public void hideRemovalIndicator(int timeToInvisible) {
        removeIndicator.animate(removeTransform, indicatorDimensions, Math.max(0, timeToInvisible - removeIndicator.remainingTime() - FADE_MILLIS));
        removeIndicator.animate(removeTransform, ZERO_SIZE, FADE_MILLIS);
    }

    // Will notify player when a piece is dropped into this holder
    public void setListener(UIPlayer player) {
        this.notify = player;
    }

    public void enableUIDrop() {
        this.uiDropEnabled = true;
    }

    public void disableUIDrop() {
        this.uiDropEnabled = false;
    }

    public void enableUIRemoval() {
        if (this.drag != null) Main.UI_COORDINATOR.add(drag);
    }

    public void disableUIRemoval() {
        if (this.drag != null) Main.UI_COORDINATOR.remove(drag);
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.push();
        if (drag != null) drag.draw();

        this.transform.transform(graphics);

        dropIndicator.draw();
        removeIndicator.draw();

        graphics.stroke(this.ownerColor);
        graphics.fill(this.ownerColor);
        graphics.text(label, labelPosition.x, labelPosition.y);
        graphics.noFill();
        graphics.strokeWeight(5);
        graphics.rect(paddedPosition.x, paddedPosition.y, paddedDimensions.x, paddedDimensions.y, paddedDimensions.z);
        graphics.textSize(labelPosition.z);

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

    public int handOff(PieceDraggable draggable, AnimationManager.AnimationSpeed speed) {
        if (this.drag != null) throw new IllegalStateException("Tried to hand off to an occupied PieceOfferingHolder");

        this.drag = draggable;
        this.drag.setTransform(new TransformData(this.transform.getPosition().add(dimensions.x/2, dimensions.y/2), this.drag.getBaseTransform().getRotation()));
        float newWidth = Math.min(dimensions.x * 0.6f, dimensions.y * 0.6f / PieceDrawable.HEIGHT_TO_WIDTH_RATIO);
        this.drag.setDimensions(new PVector(newWidth, newWidth, newWidth*PieceDrawable.HEIGHT_TO_WIDTH_RATIO));
        this.drag.addTarget(board);
        this.drag.addCallback(boardPlaceCallback);
        int time = this.drag.returnToBase(speed);

        return time;
    }
}
