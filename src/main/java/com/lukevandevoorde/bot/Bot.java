package com.lukevandevoorde.bot;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Bot {
    
    private float timeLimit;

    private long startTime;

    public Bot(float timeLimit) {
        this.timeLimit = timeLimit * (float)Math.pow(10, 9);
    }

    public Move nextMove(QB board, byte pieceToPlace) {
        this.startTime = System.nanoTime();

        System.out.println("Remaining pieces: " + board.remainingPieces.size());

        Move bestMove = new Move(-1, -1, -1, board.remainingPieces().iterator().next());
        int d = 0;

        while (d <= 15) {
            try {
                d += 1;
                
                ArrayList<Move> bestMoves = new ArrayList<>();
                int bestScore = Integer.MIN_VALUE;
                bestMoves.add(new Move(bestScore, -1, -1, (byte)-1));

                QB qb;
                Move move;

                placeLoop: for (int row = -1; ++row < 4;) {
                    for (int col = -1; ++col < 4;) {
                        if (board.board[row][col] != 0) continue;
                        qb = new QB(board);
                        // Perform move and check win
                        if (qb.move(pieceToPlace, row, col)) {
                            bestMoves.clear();
                            bestMoves.add(new Move(10000 + d, row, col, (byte)-1));
                            break placeLoop;
                        }

                        // Tie
                        if (qb.remainingPieces.size() == 0) {
                            bestMoves.clear();
                            bestMoves.add(new Move(0, row, col, (byte)-1));
                            break placeLoop;
                        }
                        
                        // Iterate over remaining pieces
                        for (byte pieceToOffer: qb.remainingPieces) {
                            move = new Move(alphabeta(false, qb, pieceToOffer, d-1).score, row, col, pieceToOffer);
                            if (move.score > bestScore) {
                                bestScore = move.score;
                                bestMoves.clear();
                                bestMoves.add(new Move(move.score, row, col, pieceToOffer));
                            } else if (move.score == bestScore) {
                                bestMoves.add(new Move(move.score, row, col, pieceToOffer));
                            }
                        }
                    }
                }

                bestMove = bestMoves.get((int)(Math.random()*bestMoves.size()));

                if (bestMove.score <= -10000 || bestMove.score >= 10000) {
                    System.out.println("Win or loss detected");
                    break;
                };
            } catch (TimeoutException e) {
                d -=1;
                break;
            }
        }

        if (bestMove.pieceToOffer == -1) System.out.println("Terminal state");
        System.out.println("Reached depth d=" + d);
        return bestMove;
    }

    private Move alphabeta(boolean maximizing, QB board, byte pieceToPlace, int depth) throws TimeoutException {
        if (depth <= 0) return new Move(0, -1, -1, (byte)-1);
        else if (System.nanoTime() - this.startTime >= this.timeLimit) throw new TimeoutException("Out of time");
        
        if (maximizing) {
            // int bestScore = Integer.MIN_VALUE, score, bestRow = -1, bestCol = -1;
            Move bestMove = new Move(Integer.MIN_VALUE, -1, -1, (byte)-1), move;
            QB qb;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    qb = new QB(board);
                    
                    // Perform move and check win
                    if (qb.move(pieceToPlace, row, col)) {
                        return new Move(10000 + depth, row, col, (byte)-1);
                    };
                    
                    // Tie
                    if (qb.remainingPieces.size() == 0) {
                        return new Move(0, row, col, (byte)-1);
                    };
                    
                    // Iterate over remaining pieces
                    for (byte pieceToOffer: qb.remainingPieces) {
                        move = new Move(alphabeta(false, qb, pieceToOffer, depth-1).score, row, col, pieceToOffer);
                        if (move.score > bestMove.score) {
                            bestMove = new Move(move.score, row, col, pieceToOffer);
                        }
                    }
                }
            }

            if (bestMove.pieceToOffer == -1) throw new IllegalStateException("Bad");
            return bestMove;
        } else {
            Move worstMove = new Move(Integer.MAX_VALUE, -1, -1, (byte)-1), move;
            QB qb;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    qb = new QB(board);
                    
                    // Perform move and check win
                    if (qb.move(pieceToPlace, row, col)) {
                        return new Move(-10000 - depth, row, col, (byte)-1);
                    };
                    
                    // Tie
                    if (qb.remainingPieces.size() == 0) {
                        return new Move(0, row, col, (byte)-1);
                    };
                    
                    // Iterate over remaining pieces
                    for (byte pieceToOffer: qb.remainingPieces) {
                        move = alphabeta(true, qb, pieceToOffer, depth-1);
                        if (move.score < worstMove.score) {
                            worstMove = new Move(move.score, row, col, pieceToOffer);
                        }
                    }
                }
            }

            if (worstMove.pieceToOffer == -1) throw new IllegalStateException("Bad");
            return worstMove;
        }
    }

}
