package com.lukevandevoorde.classes;

import processing.core.PGraphics;
import processing.core.PApplet;
import processing.core.PVector;
import com.lukevandevoorde.quartolayer.QuartoPiece;

public class PieceDrawable extends Drawable {

    public static final float HEIGHT_TO_WIDTH_RATIO = 2.5f;
    public static final float SHORT_TO_TALL_HEIGHT_RATIO = 0.6f;
    private static final int VERTS = 100;

    protected QuartoPiece piece;
    private int bodyColor, holeColor, grooveColor;
    protected float pieceHeight, grooveHeightLow, grooveHeightHigh, pitRadius, pitDepth;
    
    // Pieces should be 2.5 * as tall as the basewidth (for tall pieces)
    // Short pieces are 3/5 the height of tall ones (1.5 * dimensions.x)
    public PieceDrawable(Viewport graphics, TransformData transform, PVector boundingDimensions, QuartoPiece piece) {
        super(graphics, transform, boundingDimensions);
        this.piece = piece;
        this.setDimensions(boundingDimensions);
        
        if (piece.getLight()) {
            bodyColor = 0xFFCAA472;
            holeColor = 0xFF796244;
            grooveColor = 0xFFB69467;
        } else {
            bodyColor = 0xFF46342C;
            holeColor = 0xFF2A1F1A;
            grooveColor = 0xFF3F2F28;
        }
    }

    public QuartoPiece getPiece() {
        return this.piece;
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        dimensions.x = Math.min(newDimensions.x, newDimensions.y);
        dimensions.y = dimensions.x;
        dimensions.z = newDimensions.z;
        pieceHeight = piece.getTall() ? newDimensions.z : newDimensions.z * PieceDrawable.SHORT_TO_TALL_HEIGHT_RATIO;
        grooveHeightLow = piece.getTall() ? pieceHeight * 0.48f : pieceHeight * 0.8f;
        grooveHeightHigh = piece.getTall() ? pieceHeight * 0.5f : pieceHeight * 5/6;
        pitRadius = dimensions.x/3.5f;
        pitDepth = dimensions.x/2;
    }

    @Override
    public float getHeight() {
        return this.pieceHeight;
    }

    @Override
    // draws a piece with the base at the current center, with the top toward current +Z
    public void draw() {
        PGraphics graphics = this.viewport.getGraphics();
        graphics.push();

        transform.transform(graphics);
        graphics.translate(0, 0, -pieceHeight/2);

        graphics.noStroke();
        graphics.specular(bodyColor);
        graphics.fill(bodyColor);
        graphics.stroke(0);
        graphics.strokeWeight(1);

        if (piece.getSquare()) {
            for (int i = 0; i < 4; i++) {
                graphics.beginShape(PApplet.QUADS);
                graphics.vertex(dimensions.x/2, dimensions.x/2, 0);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, 0);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, grooveHeightLow);
                graphics.vertex(dimensions.x/2, dimensions.x/2, grooveHeightLow);

                graphics.fill(grooveColor);
                graphics.vertex(dimensions.x/2, dimensions.x/2, grooveHeightLow);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, grooveHeightLow);
                graphics.vertex(-0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightLow);
                graphics.vertex(0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightLow);

                graphics.vertex(0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightLow);
                graphics.vertex(-0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightLow);
                graphics.vertex(-0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightHigh);
                graphics.vertex(0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightHigh);

                graphics.vertex(0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightHigh);
                graphics.vertex(-0.9f * dimensions.x/2, 0.9f * dimensions.x/2, grooveHeightHigh);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, grooveHeightHigh);
                graphics.vertex(dimensions.x/2, dimensions.x/2, grooveHeightHigh);

                graphics.fill(bodyColor);
                graphics.vertex(dimensions.x/2, dimensions.x/2, grooveHeightHigh);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, grooveHeightHigh);
                graphics.vertex(-dimensions.x/2, dimensions.x/2, pieceHeight);
                graphics.vertex(dimensions.x/2, dimensions.x/2, pieceHeight);
                graphics.endShape();
                graphics.rotateZ(PApplet.PI/2);
            }

            if (piece.getFilled()) {
                graphics.translate(0, 0, pieceHeight);
                graphics.rect(-dimensions.x/2, -dimensions.x/2, dimensions.x, dimensions.x);
            } else {
                graphics.noStroke();
                float ang = 0, angIncrement = 2 * PApplet.PI / VERTS;
                float cornerX = dimensions.x/2, cornerY = -dimensions.x/2;
                float currX = pitRadius, currY = 0;
                int lastQuadrant = -1, quadrant;
                graphics.beginShape(PApplet.TRIANGLES);
                for (int i = 0; i < VERTS; i++) {
                    quadrant = 4 * i / VERTS;
                    if (quadrant != lastQuadrant) {
                        graphics.vertex(cornerX, cornerY, pieceHeight);
                        graphics.vertex(currX, currY, pieceHeight);
                        float temp = cornerX;
                        cornerX = -cornerY;
                        cornerY = temp;
                        graphics.vertex(cornerX, cornerY, pieceHeight);
                        lastQuadrant = quadrant;
                    }
                    graphics.vertex(currX, currY, pieceHeight);
                    graphics.vertex(cornerX, cornerY, pieceHeight);
                    ang += angIncrement;
                    currX = pitRadius * PApplet.cos(ang);
                    currY = pitRadius * PApplet.sin(ang);
                    graphics.vertex(currX, currY, pieceHeight);
                }

                graphics.endShape();
                graphics.fill(holeColor);
                graphics.translate(0, 0, pieceHeight - pitDepth);
                drawPit(graphics, pitRadius, pitDepth, VERTS);
            }
        } else {
            graphics.noStroke();
            float radius = dimensions.x/2;
            graphics.beginShape(PApplet.QUADS);
            float ang = 0;
            float angIncrement = 2*PApplet.PI / VERTS;
            float innerX1, innerY1, innerX2, innerY2;
            float frac;
            for (int i = 0; i < VERTS; i++) {
                innerX1 = radius * PApplet.cos(ang);
                innerY1 = radius * PApplet.sin(ang);
                innerX2 = radius * PApplet.cos(ang + angIncrement);
                innerY2 = radius * PApplet.sin(ang + angIncrement);
                graphics.vertex(innerX1, innerY1, 0);
                graphics.vertex(innerX1, innerY1, grooveHeightLow);
                graphics.vertex(innerX2, innerY2, grooveHeightLow);
                graphics.vertex(innerX2, innerY2, 0);

                graphics.fill(grooveColor);
                graphics.vertex(innerX1, innerY1, grooveHeightLow);
                graphics.vertex(0.9f * innerX1, 0.9f * innerY1, grooveHeightLow);
                graphics.vertex(0.9f * innerX2, 0.9f * innerY2, grooveHeightLow);
                graphics.vertex(innerX2, innerY2, grooveHeightLow);

                graphics.vertex(0.9f * innerX1, 0.9f * innerY1, grooveHeightLow);
                graphics.vertex(0.9f * innerX1, 0.9f * innerY1, grooveHeightHigh);
                graphics.vertex(0.9f * innerX2, 0.9f * innerY2, grooveHeightHigh);
                graphics.vertex(0.9f * innerX2, 0.9f * innerY2, grooveHeightLow);

                graphics.vertex(innerX1, innerY1, grooveHeightHigh);
                graphics.vertex(0.9f * innerX1, 0.9f * innerY1, grooveHeightHigh);
                graphics.vertex(0.9f * innerX2, 0.9f * innerY2, grooveHeightHigh);
                graphics.vertex(innerX2, innerY2, grooveHeightHigh);
                
                graphics.fill(bodyColor);
                graphics.vertex(innerX1, innerY1, grooveHeightHigh);
                graphics.vertex(innerX1, innerY1, pieceHeight);
                graphics.vertex(innerX2, innerY2, pieceHeight);
                graphics.vertex(innerX2, innerY2, grooveHeightHigh);

                if (!piece.getFilled()) {
                    // top washer
                    frac = pitRadius / radius;
                    graphics.vertex(innerX1, innerY1, pieceHeight);
                    graphics.vertex(frac * innerX1, frac * innerY1, pieceHeight);
                    graphics.vertex(frac * innerX2, frac * innerY2, pieceHeight);
                    graphics.vertex(innerX2, innerY2, pieceHeight);
                }
                ang += angIncrement;
            }
            graphics.endShape();
            if (!piece.getFilled()) {
                graphics.fill(holeColor);
                graphics.translate(0, 0, pieceHeight - pitDepth);
                drawPit(graphics, pitRadius, pitDepth, VERTS);
                graphics.translate(0, 0, pitDepth);
            } else {
                graphics.translate(0, 0, pieceHeight);
                graphics.ellipse(0, 0, 2*radius, 2*radius);
            }
            graphics.noFill();
            graphics.stroke(0);
            graphics.ellipse(0, 0, 2*radius, 2*radius);
            graphics.translate(0, 0, -pieceHeight+grooveHeightHigh);
            graphics.ellipse(0, 0, 2*radius, 2*radius);
            graphics.translate(0, 0, -grooveHeightHigh+grooveHeightLow);
            graphics.ellipse(0, 0, 2*radius, 2*radius);
            graphics.translate(0, 0, -grooveHeightLow);
            graphics.ellipse(0, 0, 2*radius, 2*radius);
        }
        
        graphics.pop();
    }

    private static void drawPit(PGraphics drawer, float radius, float depth, int verts) {
        drawer.pushMatrix();
        drawer.beginShape(PApplet.QUAD_STRIP);
        float ang = 0, angIncrement = 2 * PApplet.PI/verts;
        for (int i = 0; i < verts; i++) {
            drawer.vertex(radius * PApplet.cos(ang), radius * PApplet.sin(ang), 0);
            drawer.vertex(radius * PApplet.cos(ang), radius * PApplet.sin(ang), depth);
            ang += angIncrement;
        }
        drawer.vertex(radius, 0, 0);
        drawer.vertex(radius, 0, depth);
        drawer.endShape();
        drawer.stroke(0);
        drawer.strokeWeight(1);
        drawer.ellipse(0, 0, 2*radius, 2*radius);
        drawer.noFill();
        drawer.translate(0, 0, depth);
        drawer.ellipse(0, 0, 2*radius, 2*radius);
        drawer.popMatrix();
    }
}
