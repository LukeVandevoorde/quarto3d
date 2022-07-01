import java.util.Set;

public interface QuartoBoard {
    
    // returns true if the board has a piece at (x, y) in range [0, 3]
    public boolean pieceAt(int x, int y);

    // returns the piece at (x, y) in range [0, 3]
    public QuartoPiece getPiece(int x, int y);

    // returns a set of all QuartoPieces not currently on the board
    public Set<QuartoPiece> getRemainingPieces();
    
    // puts quartoPiece down on the board and returns true, or returns false if (x, y) is occupied
    public boolean placePiece(int x, int y, QuartoPiece quartoPiece);
}
