package com.lukevandevoorde.bot;

import java.sql.Time;
import java.util.concurrent.TimeoutException;

public class Bot {
    
    private float timeLimit;

    private byte pieceGiveReturn;
    private int rowReturn, colReturn;
    private long startTime;

    public Bot(float timeLimit) {
        this.timeLimit = timeLimit * (float)Math.pow(10, 9);
        pieceGiveReturn = 0;
        rowReturn = -1;
        colReturn = -1;
    }

    public Move nextMove(QB board, byte pieceToPlace) {
        this.startTime = System.nanoTime();

        Move bestMove = new Move(-1, -1, board.remainingPieces().iterator().next());
        float score = 0.123f;
        int d = 1;

        // score = alphabeta(true, board, pieceToPlace, 4, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        // bestMove.row = rowReturn;
        // bestMove.col = colReturn;
        // bestMove.pieceToOffer = pieceGiveReturn;
        
        long lastTime = 0;

        while (true) {
            
        }

        // while (true) {
        //     try {
        //         score = alphabeta(true, board, pieceToPlace, d, Float.MIN_VALUE, Float.MAX_VALUE);
        //         bestMove.row = this.row;
        //         bestMove.col = this.col;
        //         bestMove.pieceToOffer = this.pieceToGive;
        //         d += 1;
        //     } catch (TimeoutException e) {
        //         System.out.println("Reached depth d=" + (d-1));
        //         System.out.println("Score: " + score);
        //         break;
        //     }
        // }
        
        // System.out.println("Score: " + score);
        // return bestMove;
    }

    private float alphabeta(boolean maximizing, QB board, byte pieceToPlace, int depth, float alpha, float beta) {
        if (depth <= 0) return 0;
        // else if (System.nanoTime() - this.startTime >= this.timeLimit) throw new TimeoutException();
        
        byte rowHazard, colHazard, diagHazard = 0;
        if (maximizing) {
            float score, bestScore = Float.MIN_VALUE;
            int bestRow = -1, bestCol = -1;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    rowHazard = board.rowHazards[row];
                    colHazard = board.colHazards[col];
                    if (row == col) diagHazard = board.eqIdxDiagHazard;
                    else if (3 - row == col) diagHazard = board.compIdxDiagHazard;
                    
                    // Check win
                    if (board.move(pieceToPlace, row, col)) {
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.rowReturn = row;
                        this.colReturn = col;
                        return 10000 + depth;
                    }
                    
                    // Tie
                    if (board.remainingPieces.size() == 0) {
                        // board.undo(row, col, )
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.rowReturn = row;
                        this.colReturn = col;
                        return 0;
                    }
                    
                    for (byte pieceToOffer: board.remainingPieces) {
                        score = alphabeta(false, board, pieceToOffer, depth-1, alpha, beta);
                        if (score > bestScore) {
                            bestScore = score;
                            bestRow = this.rowReturn;
                            bestCol = this.colReturn;
                        }
                    }
                    
                    board.undo(row, col, rowHazard, colHazard, diagHazard);
                }
            }
        } else {

        }
        return 0;
    }

}
