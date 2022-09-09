package com.lukevandevoorde.classes;

import processing.core.PVector;
import java.util.HashSet;
import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceDraggable extends Drawable implements Draggable<QuartoPiece> {

    private QuartoPiece piece;
    private HashSet<DragTarget<QuartoPiece>> targets;
    private HashSet<Draggable.CallBack> callBacks;

    private TransformData baseViewPosition, pieceRotationViewData, pieceRotationDragData;
    private AnimatedDrawable animatedPiece;
    private AnimationManager positionManager;

    public PieceDraggable(Viewport graphics, TransformData transform, PVector dimensions, QuartoPiece piece) {
        super(graphics, new TransformData(transform.getPosition(), new PVector()), dimensions);
        this.piece = piece;
        callBacks = new HashSet<Draggable.CallBack>();
        targets = new HashSet<DragTarget<QuartoPiece>>();

        baseViewPosition = new TransformData(transform.getPosition(), new PVector());    // Could maybe init this when calling super constructor somehow
        pieceRotationViewData = new TransformData(new PVector(), transform.getRotation());
        pieceRotationDragData = new TransformData();

        positionManager = new AnimationManager(new TransformData(transform.getPosition(), new PVector()), dimensions);

        PieceDrawable pieceDrawable = new PieceDrawable(graphics, pieceRotationViewData, dimensions, piece);
        animatedPiece = new AnimatedDrawable(pieceDrawable);
        Main.MOUSE_COORDINATOR.add(this);
    }

    @Override
    public void setTransform(TransformData newTransform) {
        this.baseViewPosition.set(new TransformData(newTransform.getPosition(), new PVector()));
        this.pieceRotationViewData.set(new TransformData(new PVector(), newTransform.getRotation()));
    }

    @Override
    public void draw() {
        viewport.getGraphics().pushMatrix();
        positionManager.currentTransform().transform(viewport.getGraphics());
        animatedPiece.draw();
        viewport.getGraphics().popMatrix();
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        animatedPiece.setDimensions(newDimensions);
    }

    @Override
    public void addCallback(Draggable.CallBack callBack) {
        callBacks.add(callBack);
    }

    @Override
    public void addTarget(DragTarget<QuartoPiece> target) {
        targets.add(target);
    }

    @Override
    public QuartoPiece getPayload() {
        return this.piece;
    }

    @Override
    public void startDrag() {
        animatedPiece.animate(pieceRotationDragData, dimensions, 350);
        for (Draggable.CallBack c: callBacks) {
            c.onStartDrag();
        }
    }

    @Override
    public void update() {
        float scale = viewport.getScale(transform.getZ() + animatedPiece.transform.getZ());
        
        transform.setX((viewport.width()/2) + (Main.MOUSE_COORDINATOR.getMouseX() - viewport.width()/2)/scale);
        transform.setY((viewport.height()/2) + (Main.MOUSE_COORDINATOR.getMouseY() - viewport.height()/2)/scale);

        for (DragTarget<QuartoPiece> t: targets) {
            t.mouseHover(Main.MOUSE_COORDINATOR.getMouseX(), Main.MOUSE_COORDINATOR.getMouseY());
        }

        positionManager.flushSetTransform(transform);
    }

    @Override
    public void endDrag() {
        boolean accepted = false;

        for (DragTarget<QuartoPiece> target: targets) {
            accepted = target.accept(this);
            if (accepted) {
                break;
            }
        }

        if (accepted) {
            Main.MOUSE_COORDINATOR.remove(this);
            for (Draggable.CallBack c: callBacks) {
                c.onAccept();
            }
        } else {
            for (Draggable.CallBack c: callBacks) {
                c.onReject();
            }
        }
        animatedPiece.animate(animatedPiece.getCurrentTransform(), animatedPiece.getCurrentDimensions(), 0);
        animatedPiece.skipAnimation();
        animatedPiece.animate(pieceRotationViewData, dimensions, 350);
        positionManager.enqueueAnimation(baseViewPosition, dimensions, 350);
    }

    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        boolean hovering = false;
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
        PVector test = new PVector();
        viewport.getGraphics().pushMatrix();
        positionManager.currentTransform().transform(viewport.getGraphics());
        
        viewport.getGraphics().translate(0, 0, -animatedPiece.getHeight() / 4);
        test.x = viewport.getGraphics().screenX(0, 0, 0);
        test.y = viewport.getGraphics().screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        viewport.getGraphics().translate(0, 0, animatedPiece.getHeight() / 2);
        test.x = viewport.getGraphics().screenX(0, 0, 0);
        test.y = viewport.getGraphics().screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        viewport.getGraphics().popMatrix();
        return hovering;
    }
}
