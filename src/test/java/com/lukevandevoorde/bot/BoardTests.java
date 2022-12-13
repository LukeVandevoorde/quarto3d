package com.lukevandevoorde.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BoardTests {
    
    @Test
    public void testBoardRowWin() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);
        
        assertFalse(board.move(p1, 3, 0));
        assertFalse(board.move(p2, 3, 1));
        assertFalse(board.move(p3, 3, 2));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardColWin() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 3));
        assertFalse(board.move(p2, 1, 3));
        assertFalse(board.move(p3, 2, 3));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardEqDiagWin() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 0));
        assertFalse(board.move(p2, 1, 1));
        assertFalse(board.move(p3, 2, 2));
        assertTrue(board.move(p4, 3, 3));
    }

    @Test
    public void testBoardCompDiagWin() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);
        
        assertFalse(board.move(p1, 0, 3));
        assertFalse(board.move(p2, 1, 2));
        assertFalse(board.move(p3, 2, 1));
        assertTrue(board.move(p4, 3, 0));
    }

    @Test
    public void testHazardsRow() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);
        
        board.move(p1, 0, 0);
        byte[] hazards = board.getHazards(0, 0);
        assertEquals(hazards[0], (byte)0b10101001);

        board.move(p2, 0, 1);
        hazards = board.getHazards(0, 1);
        assertEquals(hazards[0], (byte)0b10100001);

        board.move(p3, 0, 2);
        hazards = board.getHazards(0, 2);
        assertEquals(hazards[0], (byte)0b10000001);

        board.move(p4, 0, 3);
        hazards = board.getHazards(0, 3);
    }

    @Test
    public void testUndo() {
        QB board = new QB();
        byte p1 = QB.piece(true, false, false, false);
        byte p2 = QB.piece(true, true, false, false);
        byte p3 = QB.piece(true, false, true, false);
        byte p4 = QB.piece(true, false, false, true);

        board.move(p1, 0, 0);
        board.move(p2, 0, 1);
        board.move(p3, 0, 2);

        byte[] hazards = board.getHazards(0, 2);
        board.move(p4, 0, 3);
        
        
    }

}
