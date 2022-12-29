package com.lukevandevoorde.classes;

import processing.core.PGraphics;
import processing.core.PVector;
import java.util.HashSet;
import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceDraggable extends Drawable implements Draggable<PieceDraggable> {

    private QuartoPiece piece;
    private HashSet<DragTarget<PieceDraggable>> targets;
    private HashSet<Draggable.CallBack> callBacks;

    private TransformData baseViewPosition, baseViewRotation, pieceRotationDragData;
    private AnimatedDrawable animatedPiece;
    private AnimationManager positionManager;

    public PieceDraggable(Viewport graphics, TransformData transform, PVector dimensions, QuartoPiece piece) {
        super(graphics, new TransformData(transform.getPosition(), new PVector()), dimensions);
        this.piece = piece;
        callBacks = new HashSet<Draggable.CallBack>();
        targets = new HashSet<DragTarget<PieceDraggable>>();

        baseViewPosition = new TransformData(transform.getPosition(), new PVector());    // Could maybe init this when calling super constructor somehow
        baseViewRotation = new TransformData(new PVector(), transform.getRotation());
        pieceRotationDragData = new TransformData();

        positionManager = new AnimationManager(new TransformData(transform.getPosition(), new PVector()), dimensions);

        PieceDrawable pieceDrawable = new PieceDrawable(graphics, baseViewRotation, dimensions, piece);
        animatedPiece = new AnimatedDrawable(pieceDrawable);
        Main.MOUSE_COORDINATOR.add(this);
    }

    public QuartoPiece getPiece() {
        return this.piece;
    }

    public TransformData getBaseTransform() {
        return new TransformData(baseViewPosition.getPosition(), baseViewRotation.getRotation());
    }

    public TransformData currentTransform() {
        return new TransformData(positionManager.currentTransform().getPosition(), animatedPiece.getCurrentTransform().getRotation());
    }

    public PVector getDimensions() {
        return this.animatedPiece.getCurrentDimensions();
    }

    @Override
    public void setTransform(TransformData newTransform) {
        this.baseViewPosition.set(new TransformData(newTransform.getPosition(), new PVector()));
        this.baseViewRotation.set(new TransformData(new PVector(), newTransform.getRotation()));
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
        this.dimensions.set(newDimensions);
    }

    @Override
    public void addCallback(Draggable.CallBack callBack) {
        callBacks.add(callBack);
    }

    @Override
    public void addTarget(DragTarget<PieceDraggable> target) {
        targets.add(target);
    }

    @Override
    public PieceDraggable getPayload() {
        return this;
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
        
        transform.setX((viewport.width()/2) + (viewport.effectiveX(Main.MOUSE_COORDINATOR.getMouseX()) - viewport.width()/2)/scale);
        transform.setY((viewport.height()/2) + (viewport.effectiveY(Main.MOUSE_COORDINATOR.getMouseY()) - viewport.height()/2)/scale);

        for (DragTarget<PieceDraggable> t: targets) {
            if (t.willAccept(this)) break;
        }

        positionManager.flushSetTransform(transform);
    }

    @Override
    public void endDrag() {
        boolean willAccept = false;
        DragTarget<PieceDraggable> t = null;

        for (DragTarget<PieceDraggable> target: targets) {
            if (willAccept = (t = target).willAccept(this)) {
                break;
            }
        }

        if (willAccept) {
            Main.MOUSE_COORDINATOR.remove(this);
            for (Draggable.CallBack c: callBacks) {
                c.onAccept();
            }

            targets.clear();
            callBacks.clear();

            t.accept(this);
        } else {
            for (Draggable.CallBack c: callBacks) {
                c.onReject();
            }
        }
        
        animatedPiece.animate(animatedPiece.getCurrentTransform(), animatedPiece.getCurrentDimensions(), 0);
        animatedPiece.skipAnimation();
        animatedPiece.animate(baseViewRotation, dimensions, 350);
        positionManager.enqueueAnimation(baseViewPosition, dimensions, 350);
    }

    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        boolean hovering = false;
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
        PVector test = new PVector();

        PGraphics graphics = this.viewport.getGraphics();
        graphics.pushMatrix();
        positionManager.currentTransform().transform(graphics);
        
        graphics.translate(0, 0, -animatedPiece.getHeight() / 4);
        test.x = graphics.screenX(0, 0, 0);
        test.y = graphics.screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        graphics.translate(0, 0, animatedPiece.getHeight() / 2);
        test.x = graphics.screenX(0, 0, 0);
        test.y = graphics.screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        graphics.popMatrix();
        return hovering;
    }
}
