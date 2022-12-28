package com.lukevandevoorde.classes;

import processing.core.PGraphics;
import processing.core.PVector;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoBoard;

public class BoardDrawable extends Drawable implements DragTarget<PieceDraggable> {
    
    private static final float PIECE_WIDTH_PROPORTION = 0.14f;
    private static final float EDGE_PADDING_BIAS = 1.25f;

    private QuartoBoard quartoBoard;
    private PieceDrawable[] pieces;
    private float pieceWidth, interiorPadding, edgePadding;

    // used to id spot under hovering PieceDraggable for highlighting
    private int hoverIndex;

    public BoardDrawable(Viewport viewport, TransformData transform, PVector dimensions, QuartoBoard quartoBoard) {
        super(viewport, transform, dimensions);
        this.quartoBoard = quartoBoard;
        hoverIndex = -1;
        pieces = new PieceDrawable[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (quartoBoard.pieceAt(i, j)) {
                    pieces[4*i+j] = new PieceDrawable(viewport, new TransformData(),
                        new PVector(pieceWidth, pieceWidth, dimensions.z),
                        quartoBoard.getPiece(i, j));
                }
            }
        }
    }

    public static PVector recommendedDimensions(float width, float height) {
        return new PVector(width, height, Math.min(width, height) * PIECE_WIDTH_PROPORTION * PieceDrawable.HEIGHT_TO_WIDTH_RATIO);
    }

    public QuartoBoard getQuartoBoard() {
        return this.quartoBoard;
    }

    @Override
    public boolean willAccept(Draggable<PieceDraggable> draggable) {
        return this.mouseHover(Main.MOUSE_COORDINATOR.getMouseX(), Main.MOUSE_COORDINATOR.getMouseY());
    }

    @Override
    public boolean accept(Draggable<PieceDraggable> draggable) {
        if (hoverIndex < 0) {
            return false;
        }
        boolean accepted = quartoBoard.placePiece(hoverIndex/4, hoverIndex%4, draggable.getPayload().getPiece());
        if (accepted) {
            pieces[hoverIndex] = new PieceDrawable(viewport, new TransformData(),
                                                    new PVector(pieceWidth, pieceWidth, dimensions.z),
                                                    quartoBoard.getPiece(hoverIndex/4, hoverIndex%4));
        }
        hoverIndex = -1;
        return accepted;
    }
    
    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
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
    public void setDimensions(PVector newDimensions) {
        dimensions.x = Math.min(dimensions.x, dimensions.y);
        dimensions.y = dimensions.x;
        this.pieceWidth = dimensions.x * PIECE_WIDTH_PROPORTION;
        this.interiorPadding = dimensions.x * (1 - 4 * PIECE_WIDTH_PROPORTION) / (3 + 2 * EDGE_PADDING_BIAS);
        this.edgePadding = interiorPadding * EDGE_PADDING_BIAS;

        for (int i = 0; i < 16; i++) {
            if (pieces[i] != null) {
                pieces[i].setDimensions(new PVector(pieceWidth, pieceWidth, newDimensions.z));
            }
        }
    }

    @Override
    public void draw() {
        PGraphics graphics = viewport.getGraphics();
        graphics.pushMatrix();
        graphics.pushStyle();

        transform.transform(graphics);

        graphics.noFill();
        graphics.stroke(0);
        graphics.fill(23);
        graphics.rect(-dimensions.x/2, -dimensions.x/2, dimensions.x, dimensions.y);
        graphics.translate(edgePadding + pieceWidth/2 - dimensions.x/2, edgePadding + pieceWidth/2 - dimensions.x/2, 1);
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (hoverIndex == 4*i+j) graphics.fill(25, 240, 30);
                else graphics.fill(120, 110, 100);
                
                graphics.ellipse(0, 0, 1.5f*pieceWidth, 1.5f*pieceWidth);
                if (pieces[4*i+j] != null) {
                    graphics.translate(0, 0, pieces[4*i+j].getHeight()/2);
                    pieces[4*i+j].draw();
                    graphics.translate(0, 0, -pieces[4*i+j].getHeight()/2);
                }
                graphics.translate(interiorPadding + pieceWidth, 0, 0);
            }
            graphics.translate(-4*(interiorPadding + pieceWidth), interiorPadding + pieceWidth, 0);
        }

        graphics.popMatrix();
        graphics.popStyle();
    }
}
