import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PVector;


// public class PieceBank extends Drawable {

//     private static final floatPIECE_PROPORTION_MIN = 0.1f, PIECE_PROPORTION_MAX = 0.4f;

//     private HashSet<PieceDraggable> drags;
    
//     public PieceBank(Viewport viewport, )
// }



public class PieceBank extends Drawable {
    private static final float PIECE_PROPORTION = 0.4f;

    private ArrayList<PieceDraggable> drags;
    private float baseWidth;

    public PieceBank(Viewport viewport, TransformData transform, PVector dimensions, Set<QuartoPiece> pieces, boolean filterLight,
        DragTarget<QuartoPiece> target, Draggable.CallBack dCallBack) {
        super(viewport, transform, dimensions);
        this.baseWidth = Math.min(viewport.width()/2, viewport.height()/4) * PIECE_PROPORTION;

        this.drags = new ArrayList<PieceDraggable>();

        for (QuartoPiece p: pieces) {
            if (p.getLight() != filterLight) {
                continue;
            }
            PieceDraggable draggable = new PieceDraggable(viewport,
                                            new TransformData(pieceMapper(p).add(transform.getX(), transform.getY(), transform.getZ()), new PVector(-PApplet.PI/3,0,0)),
                                            new PVector(baseWidth, baseWidth, baseWidth * PieceDrawable.HEIGHT_TO_WIDTH_RATIO),
                                            p);
            draggable.addCallback(
                new Draggable.CallBack() {
                    public void onAccept() {
                        drags.remove(draggable);
                    }
                }
            );
            draggable.addCallback(dCallBack);
            draggable.addTarget(target);
            drags.add(draggable);
        }
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        this.baseWidth = Math.min(dimensions.x/2, dimensions.y/4) * PIECE_PROPORTION;
        for (PieceDraggable piece: drags) {
            piece.setDimensions(new PVector(baseWidth, baseWidth, PieceDrawable.HEIGHT_TO_WIDTH_RATIO * baseWidth));
        }
    }

    @Override
    public void draw() {
        viewport.getGraphics().pushMatrix();
        // transform.transform(viewport.getGraphics()); // just added transform to the pieces instead
        for (PieceDraggable piece: drags) {
            piece.draw();
        }
        viewport.getGraphics().popMatrix();
    }

    private PVector pieceMapper(QuartoPiece piece) {
        PVector pos = new PVector();
        pos.x += dimensions.x / 4;
        pos.y += dimensions.y / 8;

        if (piece.getSquare()) {
            pos.x += dimensions.x/2;
        }

        if (!piece.getFilled()) {
            pos.y += dimensions.y/4;
        }

        if (!piece.getTall()) {
            pos.y += dimensions.y/2;
        }

        return pos;
    }
}
