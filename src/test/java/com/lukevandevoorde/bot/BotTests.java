package com.lukevandevoorde.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.lukevandevoorde.quartolayer.QuartoPiece;

public class BotTests {
    
    @Test
    public void testOffering() {
        QB board = new QB();

        board.move(QuartoPiece.quartoPiece(false, false, true, false), 0, 0);
        board.move(QuartoPiece.quartoPiece(true, true, false, false), 0, 3);
        board.move(QuartoPiece.quartoPiece(false, true, false, false), 1, 2);
        board.move(QuartoPiece.quartoPiece(true, false, false, false), 1, 3);
        board.move(QuartoPiece.quartoPiece(false, false, false, false), 2, 2);
        board.move(QuartoPiece.quartoPiece(true, false, false, true), 3, 0);
        // board.move(QuartoPiece.quartoPiece(false, true, true, false), 3, 3);

        Bot bot = new Bot(1.5f);

        ArrayList<Move> bestMoves = bot.bMoves(board, QuartoPiece.quartoPiece(false, true, true, false), 2);

        assertEquals(true, bestMoves.size() > 0);

        int total = 0;
        int count = 0;

        for (Move m: bestMoves) {
            if (!(m.row == 2 && m.col == 1)) {
                total += 1;
                if (!QuartoPiece.toQuartoPiece(m.pieceToOffer).getSquare()) count += 1;
            }
        }

        System.out.println(total);
        assertEquals(0, count);
    }

    @Test
    public void testWin() {
        QB board = new QB();

        board.move(QuartoPiece.quartoPiece(false, false, true, false), 0, 0);
        board.move(QuartoPiece.quartoPiece(true, true, false, false), 0, 3);
        board.move(QuartoPiece.quartoPiece(false, true, false, false), 1, 2);
        board.move(QuartoPiece.quartoPiece(true, false, false, false), 1, 3);
        board.move(QuartoPiece.quartoPiece(false, false, false, false), 2, 2);
        board.move(QuartoPiece.quartoPiece(true, false, false, true), 3, 0);
        board.move(QuartoPiece.quartoPiece(false, true, true, false), 3, 3);

        Bot bot = new Bot(1.5f);

        Move nextMove = bot.nextMove(board, QuartoPiece.quartoPiece(false, false, false, true));

        assertEquals(true, (nextMove.row == 1 && nextMove.col == 1) || (nextMove.row == 2 && nextMove.col == 1));
    }

}
