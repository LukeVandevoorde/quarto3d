import java.util.HashSet;
import java.util.Set;

public class Board implements QuartoBoard {

    private QuartoPiece[] qps;
    private HashSet<QuartoPiece> remainingPieces;

    public Board() {
        qps = new Piece[16];

        // qps[0] = new Piece(true, true, true, true);
        // qps[1] = new Piece(true, true, true, false);
        // qps[2] = new Piece(true, true, false, true);
        // qps[3] = new Piece(true, true, false, false);
        
        // qps[4] = new Piece(true, false, true, true);
        // qps[5] = new Piece(true, false, true, false);
        // qps[6] = new Piece(true, false, false, true);
        // qps[7] = new Piece(true, false, false, false);

        // qps[8] = new Piece(false, true, true, true);
        // qps[9] = new Piece(false, true, true, false);
        // qps[10] = new Piece(false, true, false, true);
        // qps[11] = new Piece(false, true, false, false);
        
        // qps[12] = new Piece(false, false, true, true);
        // qps[13] = new Piece(false, false, true, false);
        // qps[14] = new Piece(false, false, false, true);
        // qps[15] = new Piece(false, false, false, false);

        remainingPieces = new HashSet<QuartoPiece>();

        for (int i = 0; i < 16; i++) {
            QuartoPiece piece = new Piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0);
            boolean pieceInQPS = false;
            for (int j = 0; j < 16; j++) {
                if (qps[j] != null) {
                    if (qps[j].getTall() == piece.getTall()
                        && qps[j].getFilled() == piece.getFilled()
                        && qps[j].getLight() == piece.getLight()
                        && qps[j].getSquare() == piece.getSquare()) {
                            pieceInQPS = true;
                            break;
                    }
                }
            }
            if (!pieceInQPS) {
                remainingPieces.add(piece);
            }
        }
    }

    @Override
    public boolean placePiece(int x, int y, QuartoPiece piece) {
        if (this.qps[4*x+y] != null) {
            return false;
        }

        this.qps[4*x+y] = piece;
        return true;
    }

    @Override
    public boolean pieceAt(int x, int y) {
        return qps[4*x+y] != null;
    }

    @Override
    public QuartoPiece getPiece(int x, int y) {
        return qps[4*x + y];
    }

    @Override
    public Set<QuartoPiece> getRemainingPieces() {
        return remainingPieces;
    }
}