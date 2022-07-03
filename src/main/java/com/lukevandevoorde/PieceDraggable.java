package com.lukevandevoorde;

import processing.core.PVector;
import java.util.HashSet;

public class PieceDraggable extends Drawable implements Draggable<QuartoPiece> {

    private QuartoPiece piece;
    private AnimatedDrawable animatedPiece;
    private HashSet<DragTarget<QuartoPiece>> targets;
    private HashSet<Draggable.CallBack> callBacks;
    private TransformData tempData, tempAnimateData;
    
    public PieceDraggable(Viewport graphics, TransformData transform, PVector dimensions, QuartoPiece piece) {
        super(graphics, new TransformData(transform.getPosition(), new PVector()), dimensions);
        this.piece = piece;
        PieceDrawable pieceDrawable = new PieceDrawable(graphics, new TransformData(new PVector(), transform.getRotation()), dimensions, piece);
        animatedPiece = new AnimatedDrawable(pieceDrawable);
        callBacks = new HashSet<Draggable.CallBack>();
        targets = new HashSet<DragTarget<QuartoPiece>>();
        Main.MOUSE_COORDINATOR.add(this);
    }

    @Override
    public void setTransform(TransformData newTransform) {
        this.transform = newTransform;
        // animatedPiece.setTransform(newTransform);
    }

    @Override
    public void draw() {
        viewport.getGraphics().pushMatrix();
        this.transform.transform(viewport.getGraphics());
        animatedPiece.draw();
        viewport.getGraphics().popMatrix();
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        // this.setDimensions(newDimensions);
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
        tempData = new TransformData(new PVector(transform.getX(), transform.getY(), transform.getZ()), new PVector(transform.getRotX(), transform.getRotY(), transform.getRotZ()));
        tempAnimateData = animatedPiece.transform;
        animatedPiece.animate(new TransformData(new PVector(0, 0, 0), new PVector()), dimensions, 350);
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

        this.setTransform(tempData);
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
        animatedPiece.animate(tempAnimateData, dimensions, 350);
    }

    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        boolean hovering = false;
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
        PVector test = new PVector();
        viewport.getGraphics().pushMatrix();
        this.transform.transform(viewport.getGraphics());
        
        viewport.getGraphics().translate(0, 0, -animatedPiece.getHeight() / 4);
        test.x = viewport.getGraphics().screenX(0, 0, 0);
        test.y = viewport.getGraphics().screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(transform.getZ()) * animatedPiece.getWidth();

        viewport.getGraphics().translate(0, 0, animatedPiece.getHeight() / 2);
        test.x = viewport.getGraphics().screenX(0, 0, 0);
        test.y = viewport.getGraphics().screenY(0, 0, 0);
        hovering = hovering || m.dist(test) <= viewport.getScale(transform.getZ()) * animatedPiece.getWidth();

        viewport.getGraphics().popMatrix();
        return hovering;
    }
}
