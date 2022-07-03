package com.lukevandevoorde;

import processing.core.PGraphics;
import processing.core.PVector;

public class BoardDrawable extends Drawable implements DragTarget<QuartoPiece> {
    
    private static final float PIECE_WIDTH_PROPORTION = 0.14f;
    private static final float EDGE_PADDING_BIAS = 1.25f;

    private QuartoBoard quartoBoard;
    private PieceDrawable[] pieces;
    private float pieceWidth, interiorPadding, edgePadding;

    // a little hacky
    private int lastIndex;

    public BoardDrawable(Viewport viewport, TransformData transform, PVector dimensions, QuartoBoard quartoBoard) {
        super(viewport, transform, dimensions);
        this.quartoBoard = quartoBoard;
        lastIndex = -1;
        pieces = new PieceDrawable[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (quartoBoard.pieceAt(i, j)) {
                    pieces[4*i+j] = new PieceDrawable(viewport, TransformData.ZERO_TRANSFORM,
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
    public boolean accept(Draggable<QuartoPiece> draggable) {
        if (lastIndex < 0) {
            return false;
        }
        boolean accepted = quartoBoard.placePiece(lastIndex/4, lastIndex%4, draggable.getPayload());
        lastIndex = -1;
        return accepted;
    }

    
    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
        // m.x -= transform.getX();
        // m.y -= transform.getY();
        // m.rotate(transform.getRotZ());
        // m.x += transform.getX();
        // m.y += transform.getY();
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
                    lastIndex = 4*i+j;
                    viewport.getGraphics().popMatrix();
                    return true;
                }
                viewport.getGraphics().translate(pieceWidth + interiorPadding, 0);
            }
            viewport.getGraphics().translate(-4*(pieceWidth + interiorPadding), pieceWidth + interiorPadding);
        }
        lastIndex = -1;
        viewport.getGraphics().popMatrix();
        return false;
    }

    /*
    @Override
    public boolean mouseHover(int mouseX, int mouseY) {
        PVector m = viewport.getLocalPoint(new PVector(mouseX, mouseY));
        m.x -= transform.getX();
        m.y -= transform.getY();
        m.rotate(transform.getRotZ());
        float scale = viewport.getScale(transform.getZ());
        PVector testPoint = new PVector(-boardWidth/2 + edgePadding + pieceWidth/2, -boardWidth/2 + edgePadding + pieceWidth/2);
        testPoint.x *= scale;
        testPoint.y *= scale;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (this.pieces[4*i+j] == null && m.dist(testPoint) <= scale*1.5f*pieceWidth/2) {
                    lastIndex = 4*i+j;
                    return true;
                }
                testPoint.x += scale * (pieceWidth + interiorPadding);
            }
            testPoint.x -= scale*4*(pieceWidth + interiorPadding);
            testPoint.y += scale*(pieceWidth + interiorPadding);
            // viewport.getGraphics().translate(4*(pieceWidth + interiorPadding), pieceWidth + interiorPadding);
        }
        lastIndex = -1;
        return false;
    }*/

    @Override
    public void setDimensions(PVector newDimensions) {
        dimensions.x = Math.min(dimensions.x, dimensions.y);
        dimensions.y = dimensions.x;
        this.pieceWidth = dimensions.x * PIECE_WIDTH_PROPORTION;
        this.interiorPadding = dimensions.x * (1 - 4 * PIECE_WIDTH_PROPORTION) / (3 + 2 * EDGE_PADDING_BIAS);
        this.edgePadding = interiorPadding * EDGE_PADDING_BIAS;
        // this.pieceSpacing = boardWidth * PIECE_SPACING_PROPORTION;
        // this.edgeSpacing = boardWidth * (1 - 3 * PIECE_SPACING_PROPORTION) / 2;

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
        // graphics.rect(-boardWidth/2, -boardWidth/2, boardWidth, boardWidth);

        graphics.fill(23);
        graphics.rect(-dimensions.x/2, -dimensions.x/2, dimensions.x, dimensions.y);
        
        graphics.translate(-dimensions.x/2, -dimensions.x/2, 1);

        // graphics.translate(pieceBuffer + pieceCenterGap/2, pieceBuffer + pieceCenterGap/2, 0);
        graphics.translate(edgePadding + pieceWidth/2, edgePadding + pieceWidth/2, 0);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                graphics.fill(120, 110, 100);
                if (lastIndex == 4*i+j) {
                    graphics.fill(25, 240, 30);
                }
                // graphics.circle(0, 0, 1.5f*pieceWidth);
                graphics.ellipse(0, 0, 1.5f*pieceWidth, 1.5f*pieceWidth);
                if (quartoBoard.pieceAt(i, j)) {
                    if (pieces[4*i + j] == null) {
                        pieces[4*i + j] = new PieceDrawable(viewport, TransformData.ZERO_TRANSFORM,
                                                            new PVector(pieceWidth, pieceWidth, dimensions.z),
                                                            quartoBoard.getPiece(i, j));
                    }
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
