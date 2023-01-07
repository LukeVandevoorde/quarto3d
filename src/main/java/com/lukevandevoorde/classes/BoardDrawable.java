package com.lukevandevoorde.classes;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.HashSet;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.classes.AnimationManager.AnimationSpeed;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoBoardState;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class BoardDrawable extends Drawable implements Draggable<Void>, DragTarget<QuartoPiece> {
    
    private static final float PIECE_WIDTH_PROPORTION = 0.14f;
    private static final float EDGE_PADDING_BIAS = 1.25f;

    private Drawable[] pieces;
    private float pieceWidth, interiorPadding, edgePadding;
    private AnimationManager manager;
    private TransformData userView, selectView;
    private int pieceDropEndTime;
    private boolean inUserView;

    // highlight spot under hovering PieceDraggable to make placement obvious
    private int hoverIndex;
    // highlight last placed piece
    private int lastPlacementIndex;
    // highlight pieces in winning rows
    private HashSet<Integer> windices;
    private int winHighlightColor;

    private QuartoPiece lastPlacementPiece;
    private UIPlayer notify;

    public BoardDrawable(Viewport viewport, TransformData userView, TransformData selectView, PVector dimensions, QuartoBoardState initialState) {
        super(viewport, new TransformData(userView), new PVector(dimensions.x, dimensions.y, dimensions.z));
        manager = new AnimationManager(userView, dimensions);

        this.userView = userView;
        this.selectView = selectView;
        this.inUserView = true;
        this.pieceDropEndTime = 0;

        pieces = new Drawable[16];
        setDimensions(dimensions);
        hoverIndex = -1;
        lastPlacementIndex = -1;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (initialState.pieceAt(i, j)) {
                    pieces[4*i+j] = new PieceDrawable(viewport, new TransformData(),
                        new PVector(pieceWidth, pieceWidth, dimensions.z),
                        initialState.getPiece(i, j));
                }
            }
        }

        Main.UI_COORDINATOR.add(this);
    }

    public static PVector recommendedDimensions(float width, float height) {
        return new PVector(width, height, Math.min(width, height) * PIECE_WIDTH_PROPORTION * PieceDrawable.HEIGHT_TO_WIDTH_RATIO);
    }

    public void requestNotification(UIPlayer player) {
        this.notify = player;
    }

    public QuartoPiece getLastPlacementPiece() {
        return this.lastPlacementPiece;
    }

    public void highlightWin(int[][] winningCoords, int highlightColor) {
        windices = new HashSet<>();
        for (int i = 0; i < winningCoords[0].length; i++) {
            windices.add(4*winningCoords[0][i] + winningCoords[1][i]);
        }
        this.winHighlightColor = highlightColor;
    }

    public int enterSelectView(AnimationManager.AnimationSpeed speed) {
        if (!inUserView) return 0;
        inUserView = false;

        selectView.setRotZ(((int)((transform.getRotZ()) / PConstants.HALF_PI + 0.5f)) * PConstants.HALF_PI);
        TransformData ct = manager.currentTransform();
        manager.enqueueAnimation(ct, manager.currentDimensions(), 0);
        manager.flush();
        int time = AnimationManager.calcAnimationTime(speed, ct, selectView);
        manager.enqueueAnimation(selectView, null, time);
        return time;
    }

    public void adjustDistance(int amount) {
        if (manager.animating()) return;
        userView.setZ(Math.max(Math.min(userView.getZ() - amount*viewport.height()/20, 0), -2*viewport.height()));
        manager.enqueueAnimation(userView, null, 0);
    }

    public int enterUserView(AnimationSpeed speed) {
        if (inUserView) return 0;
        inUserView = true;

        // Wait for dropped pieces to finish dropping
        int diff = Main.TIME_KEEPER.millis() - (pieceDropEndTime);
        if (diff < 0) {
            manager.enqueueAnimation(selectView, null, -diff);
            TransformData ct = manager.currentTransform();
            manager.enqueueAnimation(ct, manager.currentDimensions(), 0);
            int time = AnimationManager.calcAnimationTime(speed, ct, userView);
            manager.enqueueAnimation(userView, null, time);
            return time - Math.min(diff, 0);
        } else {
            TransformData ct = manager.currentTransform();
            manager.enqueueAnimation(ct, manager.currentDimensions(), 0);
            int time = AnimationManager.calcAnimationTime(speed, ct, userView);
            manager.flush();
            manager.enqueueAnimation(userView, null, time);
            return time - Math.min(diff, 0);
        }
    }

    public int handOff(PieceDraggable piece, int row, int col, AnimationSpeed dropSpeed) {
        PVector posDiff = this.boardPos(row, col);
        posDiff.rotate(-this.transform.getRotZ());
        posDiff.x += this.transform.getX();
        posDiff.y += this.transform.getY();
        
        TransformData pos = piece.currentTransform();

        posDiff.x = pos.getX() - posDiff.x;
        posDiff.y = pos.getY() - posDiff.y;
        posDiff.rotate(this.transform.getRotZ());
        posDiff.z = pos.getZ() - piece.getDimensions().z/2 - this.transform.getZ();
        
        TransformData diff = new TransformData(posDiff, pos.getRotation());
        AnimatedDrawable ad = new AnimatedDrawable(new PieceDrawable(viewport, diff, piece.getDimensions(), piece.getPayload()));

        Main.UI_COORDINATOR.remove(piece);

        TransformData home = new TransformData();
        int dropTime = AnimationManager.calcAnimationTime(dropSpeed, diff, home);
        ad.animate(home, new PVector(pieceWidth, pieceWidth, dimensions.z), dropTime);
        pieces[4*row+col] = ad;
        lastPlacementIndex = 4*row+col;
        pieceDropEndTime = Main.TIME_KEEPER.millis() + dropTime;
        return dropTime;
    }

    @Override
    public boolean willAccept(Draggable<QuartoPiece> draggable) {
        PVector m = viewport.getLocalPoint(new PVector(Main.UI_COORDINATOR.getMouseX(), Main.UI_COORDINATOR.getMouseY()));
        PVector testPoint = new PVector();
        viewport.getGraphics().pushMatrix();
        transform.transform(viewport.getGraphics());
        viewport.getGraphics().translate(-dimensions.x/2 + edgePadding + pieceWidth/2, -dimensions.y/2 + edgePadding + pieceWidth/2, 1);
        float scale = viewport.getScale(transform.getZ());

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                testPoint.x = viewport.getGraphics().screenX(0, 0, 0);
                testPoint.y = viewport.getGraphics().screenY(0, 0, 0);
                if (this.pieces[4*i+j] == null && m.dist(testPoint) <= scale*1.5f*pieceWidth/2) {
                    hoverIndex = 4*i+j;
                    viewport.getGraphics().popMatrix();
                    return true;
                }
                viewport.getGraphics().translate(pieceWidth + interiorPadding, 0);
            }
            viewport.getGraphics().translate(-4*(pieceWidth + interiorPadding), pieceWidth + interiorPadding);
        }
        hoverIndex = -1;
        viewport.getGraphics().popMatrix();
        return false;
    }

    @Override
    public boolean accept(Draggable<QuartoPiece> draggable) {
        if (hoverIndex < 0) return false;
        
        if (this.notify != null) notify.notifyPlacement(hoverIndex/4, hoverIndex%4, draggable.getPayload());
        hoverIndex = -1;

        return true;
    }
    
    @Override
    public boolean mouseOver() {
        return true;
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        dimensions.x = Math.min(newDimensions.x, newDimensions.y);
        dimensions.y = dimensions.x;
        this.pieceWidth = dimensions.x * PIECE_WIDTH_PROPORTION;
        this.interiorPadding = dimensions.x * (1 - 4 * PIECE_WIDTH_PROPORTION) / (3 + 2 * EDGE_PADDING_BIAS);
        this.edgePadding = interiorPadding * EDGE_PADDING_BIAS;

        for (int i = 0; i < 16; i++) {
            if (pieces[i] != null) {
                if (pieces[i] instanceof AnimatedDrawable) {
                    AnimatedDrawable ad = (AnimatedDrawable)pieces[i];
                    ad.animate(null, new PVector(pieceWidth, pieceWidth, newDimensions.z), -1);
                } else {
                    pieces[i].setDimensions(new PVector(pieceWidth, pieceWidth, newDimensions.z));
                }
            }
        }
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.pushMatrix();
        graphics.pushStyle();

        manager.set(this);
        transform.transform(graphics);

        graphics.noFill();
        graphics.stroke(0);
        graphics.fill(23);
        graphics.rect(-dimensions.x/2, -dimensions.x/2, dimensions.x, dimensions.y, 0.05f*dimensions.x);
        graphics.translate(edgePadding + pieceWidth/2 - dimensions.x/2, edgePadding + pieceWidth/2 - dimensions.x/2, 1);
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int index = 4*i+j;
                if (index == hoverIndex) {
                    graphics.fill(25, 240, 30);
                } else if (index == lastPlacementIndex) {
                    graphics.fill(230, 220, 210);
                } else if (windices != null && windices.contains(index)) {
                    graphics.fill(winHighlightColor);
                } else {
                    graphics.fill(120, 110, 100);
                }
                graphics.ellipse(0, 0, 1.5f*pieceWidth, 1.5f*pieceWidth);
                Drawable piece = pieces[4*i+j];
                if (piece != null) {
                    // TODO: this push/pop shouldn't be necessary... without it, remaining base ellipses turn black though
                    graphics.push();
                    graphics.translate(0, 0, piece.getHeight()/2);
                    piece.draw();
                    graphics.translate(0, 0, -piece.getHeight()/2);
                    graphics.pop();
                }
                graphics.translate(interiorPadding + pieceWidth, 0, 0);
            }
            graphics.translate(-4*(interiorPadding + pieceWidth), interiorPadding + pieceWidth, 0);
        }

        graphics.popMatrix();
        graphics.popStyle();
    }

    @Override
    public void update() {
        if (manager.animating() || !inUserView) return;

        float centerX = viewport.getGraphics().screenX(userView.getX(), userView.getY(), userView.getZ()) + viewport.getPosition().x;
        float centerY = viewport.getGraphics().screenY(userView.getX(), userView.getY(), userView.getZ()) + viewport.getPosition().y;

        int mouseX = Main.UI_COORDINATOR.getMouseX(), pmouseX = Main.UI_COORDINATOR.getPrevMouseX();
        int mouseY = Main.UI_COORDINATOR.getMouseY(), pmouseY = Main.UI_COORDINATOR.getPrevMouseY();
        PVector arm = new PVector(pmouseX - centerX, pmouseY - centerY);
        PVector drag = new PVector(mouseX - pmouseX, mouseY - pmouseY);

        userView.setRotX(Math.min(0, Math.max(userView.getRotX() + 0.01f*(mouseY - pmouseY), -PConstants.HALF_PI)));
        userView.setRotZ(((userView.getRotZ() + (0.01f) * drag.cross(arm).z / arm.mag()) % PConstants.TWO_PI + PConstants.TWO_PI) % PConstants.TWO_PI);
        manager.enqueueAnimation(userView, null, 0);
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public void startDrag() {}

    @Override
    public void endDrag() {}
    
    @Override
    public void addTarget(DragTarget<Void> target) {}

    @Override
    public void addCallback(CallBack callBack) {}

    @Override
    public Void getPayload() { return null; }

    private PVector boardPos(int row, int col) {
        PVector res = new PVector(edgePadding + pieceWidth/2 - dimensions.x/2, edgePadding + pieceWidth/2 - dimensions.x/2, 1);
        res = res.add(col*(interiorPadding + pieceWidth), row*(interiorPadding + pieceWidth));
        return res;
    }
}
