package com.lukevandevoorde.classes;

import processing.core.PGraphics;
import processing.core.PVector;
import java.util.HashSet;
import com.lukevandevoorde.Main;
import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.interfaces.Clickable;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceDraggable extends Drawable implements Draggable<QuartoPiece>, Clickable {

    private static final AnimationManager.AnimationSpeed DEFAULT_RETURN_SPEED = new AnimationManager.AnimationSpeed(2000, 3);

    private QuartoPiece piece;
    private HashSet<DragTarget<QuartoPiece>> targets;
    private HashSet<Draggable.CallBack> callBacks;

    private TransformData baseViewPosition, baseViewRotation, pieceRotationDragData;
    private AnimatedDrawable animatedPiece;
    private AnimationManager positionManager;

    public PieceDraggable(Viewport graphics, TransformData transform, PVector dimensions, QuartoPiece piece) {
        super(graphics, new TransformData(transform.getPosition(), new PVector()), dimensions);
        this.piece = piece;
        callBacks = new HashSet<Draggable.CallBack>();
        targets = new HashSet<DragTarget<QuartoPiece>>();

        baseViewPosition = new TransformData(transform.getPosition(), new PVector());
        baseViewRotation = new TransformData(new PVector(), transform.getRotation());
        pieceRotationDragData = new TransformData();

        positionManager = new AnimationManager(baseViewPosition, dimensions);

        PieceDrawable pieceDrawable = new PieceDrawable(graphics, baseViewRotation, dimensions, piece);
        animatedPiece = new AnimatedDrawable(pieceDrawable);
        Main.UI_COORDINATOR.add(this);
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

    public int returnToBase(AnimationSpeed speed) {
        TransformData position = positionManager.currentTransform();
        int time = AnimationManager.calcAnimationTime(speed, position, baseViewPosition);
        animatedPiece.animate(animatedPiece.getCurrentTransform(), animatedPiece.getCurrentDimensions(), 0);
        animatedPiece.skipAnimation();
        animatedPiece.animate(baseViewRotation, dimensions, time);
        positionManager.enqueueAnimation(position, null, 0);
        positionManager.flush();
        positionManager.enqueueAnimation(baseViewPosition, dimensions, time);

        return time;
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        this.dimensions.set(newDimensions);
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
        animatedPiece.animate(pieceRotationDragData, dimensions, 200);
        for (Draggable.CallBack c: callBacks) {
            c.onStartDrag();
        }
    }

    @Override
    public void update() {
        float scale = viewport.getScale(transform.getZ() + animatedPiece.transform.getZ());
        
        transform.setX((viewport.width()/2) + (viewport.effectiveX(Main.UI_COORDINATOR.getMouseX()) - viewport.width()/2)/scale);
        transform.setY((viewport.height()/2) + (viewport.effectiveY(Main.UI_COORDINATOR.getMouseY()) - viewport.height()/2)/scale);

        for (DragTarget<QuartoPiece> t: targets) {
            if (t.willAccept(this)) break;
        }

        positionManager.flushSetTransform(transform);
    }

    @Override
    public void endDrag() {
        boolean willAccept = false;
        DragTarget<QuartoPiece> t = null;

        for (DragTarget<QuartoPiece> target: targets) {
            if (willAccept = (t = target).willAccept(this)) {
                break;
            }
        }

        if (willAccept) {
            Main.UI_COORDINATOR.remove(this);
            callBacks.forEach(c -> c.onAccept());

            targets.clear();
            callBacks.clear();

            t.accept(this);
        } else {
            callBacks.forEach(c -> c.onReject());
            returnToBase(DEFAULT_RETURN_SPEED);
        }
    }

    @Override
    public boolean mouseOver() {
        boolean hovering = false;
        PVector m = viewport.getLocalPoint(new PVector(Main.UI_COORDINATOR.getMouseX(), Main.UI_COORDINATOR.getMouseY()));
        PVector test = new PVector();

        PGraphics graphics = this.viewport.getGraphics();
        graphics.pushMatrix();
        positionManager.currentTransform().transform(graphics);
        
        graphics.translate(0, -animatedPiece.getHeight() / 4, 0);
        test.x = graphics.screenX(0, 0, 0);
        test.y = graphics.screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= 1.25f*viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        graphics.translate(0, animatedPiece.getHeight() / 2, 0);
        test.x = graphics.screenX(0, 0, 0);
        test.y = graphics.screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= 1.25f*viewport.getScale(positionManager.currentTransform().getZ()) * animatedPiece.getWidth();

        graphics.popMatrix();
        return hovering;
    }

    @Override
    public void onClick() {
        System.out.println("\tOn Click");
    }

    @Override
    public void onDoubleClick() {
        System.out.println("\tOn Double Click");
    }

    @Override
    public int priority() {
        return 2;
    }
}
