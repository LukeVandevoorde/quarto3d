package com.lukevandevoorde.bot;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Bot {
    
    private float timeLimit;

    private long startTime;

    public Bot(float timeLimit) {
        this.timeLimit = timeLimit * (float)Math.pow(10, 9);
    }

    public ArrayList<Move> bMoves(QB board, byte pieceToPlace, int depth) {
        this.startTime = System.nanoTime();

        int d = 0;

        ArrayList<Move> res = new ArrayList<>();
        while (d <= 15) {
            try {
                d += 1;
                
                ArrayList<Move> bestMoves = new ArrayList<>();
                int bestScore = Integer.MIN_VALUE, score;
                bestMoves.add(new Move(bestScore, -1, -1, (byte)-1));

                QB qb;

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
                            score = alphabeta(false, qb, pieceToOffer, d-1, bestScore, Integer.MAX_VALUE);
                            if (score > bestScore) {
                                bestScore = score;
                                bestMoves.clear();
                                bestMoves.add(new Move(score, row, col, pieceToOffer));
                            } else if (score == bestScore) {
                                bestMoves.add(new Move(score, row, col, pieceToOffer));
                            }
                        }
                    }
                }

                res = bestMoves;

                if (bestScore <= -10000 || bestScore >= 10000) {
                    break;
                };
            } catch (TimeoutException e) {
                break;
            }
        }

        return res;
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
                int bestScore = Integer.MIN_VALUE, score;
                bestMoves.add(new Move(bestScore, -1, -1, (byte)-1));

                QB qb;

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
                            score = alphabeta(false, qb, pieceToOffer, d-1, bestScore, Integer.MAX_VALUE);
                            if (score > bestScore) {
                                bestScore = score;
                                bestMoves.clear();
                                bestMoves.add(new Move(score, row, col, pieceToOffer));
                            } else if (score == bestScore) {
                                bestMoves.add(new Move(score, row, col, pieceToOffer));
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

    private int alphabeta(boolean maximizing, QB board, byte pieceToPlace, int depth, int alpha, int beta) throws TimeoutException {
        if (depth <= 0) return 0;
        else if (System.nanoTime() - this.startTime >= this.timeLimit) throw new TimeoutException("Out of time");
        
        if (maximizing) {
            int bestScore = Integer.MIN_VALUE, score;
            QB qb;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    qb = new QB(board);
                    
                    // Perform move and check win
                    if (qb.move(pieceToPlace, row, col)) return 10000 + depth;
                    // Tie
                    if (qb.remainingPieces.size() == 0) return 0;

                    // Iterate over remaining pieces
                    for (byte pieceToOffer: qb.remainingPieces) {
                        score = alphabeta(false, qb, pieceToOffer, depth-1, alpha, beta);

                        if (score > beta) return score;
                        else if (score > bestScore) bestScore = score;

                        alpha = Math.max(alpha, score);
                    }
                }
            }

            return bestScore;
        } else {
            int worstScore = Integer.MAX_VALUE, score;
            QB qb;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    qb = new QB(board);

                    // Perform move and check win
                    if (qb.move(pieceToPlace, row, col)) return -10000 - depth;
                    // Tie
                    if (qb.remainingPieces.size() == 0) return 0;

                    // Iterate over remaining pieces
                    for (byte pieceToOffer: qb.remainingPieces) {
                        score = alphabeta(true, qb, pieceToOffer, depth-1, alpha, beta);

                        if (score < alpha) return score;
                        else if (score < worstScore) worstScore = score;
                        
                        beta = Math.min(beta, score);
                    }
                }
            }

            return worstScore;
        }
    }

}
