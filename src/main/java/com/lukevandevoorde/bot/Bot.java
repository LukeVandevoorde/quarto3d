package com.lukevandevoorde.bot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.lukevandevoorde.quartolayer.QuartoPiece;

public class Bot {
    
    private static final List<Byte> allBytes = Arrays.asList(QuartoPiece.allBytes().toArray(new Byte[0]));

    private float timeLimit;

    private byte returnOffering;
    private int returnRow, returnCol;
    private long startTime;

    public Bot(float timeLimit) {
        this.timeLimit = timeLimit * (float)Math.pow(10, 9);
        returnOffering = 0;
        returnRow = -1;
        returnCol = -1;
    }

    public Move nextMove(QB board, byte pieceToPlace) {
        this.startTime = System.nanoTime();

        Move bestMove = new Move(-1, -1, board.remainingPieces().iterator().next());
        int score = -1;
        int d = 1;

        while (d <= 16) {
            try {
                score = alphabeta(true, board, pieceToPlace, d, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
                bestMove.row = this.returnRow;
                bestMove.col = this.returnCol;
                // if (Math.abs(score) < 9000) 
                bestMove.pieceToOffer = this.returnOffering;
                d += 1;
            } catch (TimeoutException e) {
                break;
            }
        }
        
        System.out.println("Reached depth d=" + d);
        System.out.println("Score: " + score);
        return bestMove;
    }

    private int alphabeta(boolean maximizing, QB board, byte pieceToPlace, int depth, float alpha, float beta) throws TimeoutException {
        if (depth <= 0) return 0;
        else if (System.nanoTime() - this.startTime >= this.timeLimit) throw new TimeoutException("Out of time");
        
        byte rowHazard, colHazard, diagHazard = 0, pieceToOffer;
        int score;
        if (maximizing) {
            score = Integer.MIN_VALUE;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    rowHazard = board.rowHazards[row];
                    colHazard = board.colHazards[col];
                    if (row == col) diagHazard = board.eqIdxDiagHazard;
                    else if (3 - row == col) diagHazard = board.compIdxDiagHazard;
                    
                    // Perform move and check win
                    if (board.move(pieceToPlace, row, col)) {
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.returnRow = row;
                        this.returnCol = col;
                        // System.out.println("win");
                        return 10000 + depth;
                    }
                    
                    // Tie
                    if (board.remainingPieces.size() == 0) {
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.returnRow = row;
                        this.returnCol = col;
                        // System.out.println("max tie");
                        return 0;
                    }
                    
                    // Iterate over remaining pieces
                    for (int i = -1; ++i < 16; ) {
                        if (!board.remainingPieces.contains(pieceToOffer = allBytes.get(i))) continue;
                        score = alphabeta(false, board, pieceToOffer, depth-1, alpha, beta);
                        if (score >= beta) {
                            this.returnRow = row;
                            this.returnCol = col;
                            this.returnOffering = pieceToOffer;
                            board.undo(row, col, rowHazard, colHazard, diagHazard);
                            // System.out.println("beta cutoff");
                            return score;
                        }
                        alpha = Math.max(alpha, score);
                    }

                    board.undo(row, col, rowHazard, colHazard, diagHazard);
                }
            }
        } else {
            score = Integer.MAX_VALUE;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    rowHazard = board.rowHazards[row];
                    colHazard = board.colHazards[col];
                    if (row == col) diagHazard = board.eqIdxDiagHazard;
                    else if (3 - row == col) diagHazard = board.compIdxDiagHazard;
                    
                    // Perform move and check win
                    if (board.move(pieceToPlace, row, col)) {
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.returnRow = row;
                        this.returnCol = col;
                        // System.out.println("loss");
                        return -10000 - depth;
                    }
                    
                    // Tie
                    if (board.remainingPieces.size() == 0) {
                        board.undo(row, col, rowHazard, colHazard, diagHazard);
                        this.returnRow = row;
                        this.returnCol = col;
                        // System.out.println("min tie");
                        return 0;
                    }
                    
                    // Iterate over remaining pieces
                    for (int i = -1; ++i < 16; ) {
                        if (!board.remainingPieces.contains(pieceToOffer = allBytes.get(i))) continue;
                        score = alphabeta(true, board, pieceToOffer, depth-1, alpha, beta);
                        if (score <= alpha) {
                            this.returnRow = row;
                            this.returnCol = col;
                            this.returnOffering = pieceToOffer;
                            board.undo(row, col, rowHazard, colHazard, diagHazard);
                            System.out.println("alpha cutoff");
                            return score;
                        }
                        beta = Math.min(beta, score);
                    }

                    board.undo(row, col, rowHazard, colHazard, diagHazard);
                }
            }
        }
        
        return score;
    }

}
