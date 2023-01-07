package com.lukevandevoorde.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.lukevandevoorde.quartolayer.QuartoPiece;

public class BoardTests {

    @Test
    public void testMoveBoard() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 1, 0));
        assertFalse(board.move(p2, 2, 2)); // eq diag
        assertFalse(board.move(p3, 0, 3)); // comp diag
        assertFalse(board.move(p4, 3, 1));

        assertEquals(board.pieceAt(0, 0), 0);
        assertEquals(board.pieceAt(1, 0), p1);
        assertEquals(board.pieceAt(2, 2), p2);
        assertEquals(board.pieceAt(0, 3), p3);
        assertEquals(board.pieceAt(3, 1), p4); 
    }

    @Test
    public void testMoveHazards() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 1, 0));
        assertFalse(board.move(p2, 2, 2)); // eq diag
        assertFalse(board.move(p3, 0, 3)); // comp diag
        assertFalse(board.move(p4, 3, 1));
        
        byte[] hazards;
        
        hazards = board.getHazards(0, 0); // eq diag
        assertEquals((byte)(p3 & -1), hazards[0]);
        assertEquals((byte)(p1 & -1), hazards[1]);
        assertEquals((byte)(p2 & -1), hazards[2]);

        hazards = board.getHazards(2, 1); // comp diag
        assertEquals((byte)(p2 & -1), hazards[0]);
        assertEquals((byte)(p4 & -1), hazards[1]);
        assertEquals((byte)(p3 & -1), hazards[2]);

        hazards = board.getHazards(3, 2);
        assertEquals((byte)(p4 & -1), hazards[0]);
        assertEquals((byte)(p2 & -1), hazards[1]);

        hazards = board.getHazards(1, 3);
        assertEquals((byte)(p1 & -1), hazards[0]);
        assertEquals((byte)(p3 & -1), hazards[1]);
    }

    @Test
    public void testMoveCounts() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 1, 0));
        assertFalse(board.move(p2, 2, 2)); // eq diag
        assertFalse(board.move(p3, 0, 3)); // comp diag
        assertFalse(board.move(p4, 3, 1));
        
        int[] counts;
        
        counts = board.getCounts(0, 0); // eq diag
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
        assertEquals(1, counts[2]);

        counts = board.getCounts(2, 1); // comp diag
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
        assertEquals(1, counts[2]);

        counts = board.getCounts(3, 2);
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);

        counts = board.getCounts(1, 3);
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
    }
    
    @Test
    public void testBoardRowWin() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 3, 0));
        assertFalse(board.move(p2, 3, 1));
        assertFalse(board.move(p3, 3, 2));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardColWin() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 3));
        assertFalse(board.move(p2, 1, 3));
        assertFalse(board.move(p3, 2, 3));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardEqDiagWin() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 0));
        assertFalse(board.move(p2, 1, 1));
        assertFalse(board.move(p3, 2, 2));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardCompDiagWin() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 3));
        assertFalse(board.move(p2, 1, 2));
        assertFalse(board.move(p3, 2, 1));
        assertTrue(board.move(p4, 3, 0));
    }

    @Test
    public void testHazardsRow() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);
        
        board.move(p1, 0, 0);
        byte[] hazards = board.getHazards(0, 0);
        assertEquals((byte)0b10101001, hazards[0]);

        board.move(p2, 0, 1);
        hazards = board.getHazards(0, 1);
        assertEquals((byte)0b10100001, hazards[0]);

        board.move(p3, 0, 2);
        hazards = board.getHazards(0, 2);
        assertEquals((byte)0b10000001, hazards[0]);

        board.move(p4, 0, 3);
        hazards = board.getHazards(0, 3);
        assertEquals((byte)0b00000001, hazards[0]);
    }

    @Test
    public void testUndo() {
        QB board = new QB();
        byte p1 = QuartoPiece.quartoPiece(true, false, false, false);
        byte p2 = QuartoPiece.quartoPiece(true, true, false, false);
        byte p3 = QuartoPiece.quartoPiece(true, false, true, false);
        byte p4 = QuartoPiece.quartoPiece(true, false, false, true);

        board.move(p1, 0, 0);
        board.move(p2, 0, 1);
        byte[] h1 = board.getHazards(0, 2);
        board.move(p3, 0, 2);
        byte[] h2 = board.getHazards(0, 3);
        board.move(p4, 0, 3);
        
        assertEquals(12, board.remainingPieces().size());

        byte[] hazards;
        int[] counts;
        // Undo 1
        board.undo(0, 3, h2[0], h2[1], h2[2]);
        hazards = board.getHazards(0, 3);
        counts = board.getCounts(0, 3);
        assertEquals((byte)0b10000001, hazards[0]);
        assertEquals((byte)(-1), hazards[1]);
        assertEquals((byte)(-1), hazards[2]);
        assertEquals(3, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(0, counts[2]);
        assertEquals(13, board.remainingPieces().size());

        // Undo 2
        board.undo(0, 2, h1[0], h1[1], (byte)0);
        hazards = board.getHazards(0, 2);
        counts = board.getCounts(0, 2);
        assertEquals((byte)0b10100001, hazards[0]);
        assertEquals((byte)(-1), hazards[1]);
        assertEquals(2, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(14, board.remainingPieces().size());
    }

}
