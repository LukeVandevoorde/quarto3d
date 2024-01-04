package com.lukevandevoorde.bot;

import java.util.ArrayList;
import java.util.HashSet;
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
                        for (byte pieceToOffer: new HashSet<Byte>(qb.remainingPieces)) {
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
        if (System.nanoTime() - this.startTime >= this.timeLimit) throw new TimeoutException("Out of time");
        
        byte prevEqDiagHazard = board.eqIdxDiagHazard;
        byte prevCompDiagHazard = board.compIdxDiagHazard;
        int prevEqDiagCount = board.eqIdxDiagCount;
        int prevCompDiagCount = board.compIdxDiagCount;
        if (maximizing) {
            if (depth <= 0) return heuristic2(board);
            int bestScore = Integer.MIN_VALUE, score;
            byte rowHazard, colHazard;
            int rowCount, colCount;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;
                    
                    // qb = new QB(board);
                    rowHazard = board.rowHazards[row];
                    colHazard = board.colHazards[col];
                    rowCount = board.rowCounts[row];
                    colCount = board.colCounts[col];
                    
                    // Perform move and check win
                    if (board.move(pieceToPlace, row, col)) {
                        board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                        return 10000 + depth;
                    }
                    // Tie
                    if (board.remainingPieces.size() == 0) {
                        board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                        return 0;
                    }
                    // Iterate over remaining pieces
                    for (byte pieceToOffer: new HashSet<Byte>(board.remainingPieces)) {
                        score = alphabeta(false, board, pieceToOffer, depth-1, alpha, beta);

                        if (score > beta) {
                            board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                            return score;
                        } else if (score > bestScore) bestScore = score;

                        alpha = Math.max(alpha, score);
                    }
                
                    board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                }
            }

            return bestScore;
        } else {
            if (depth <= 0) return -heuristic2(board);
            int worstScore = Integer.MAX_VALUE, score;
            byte rowHazard, colHazard;
            int rowCount, colCount;
            for (int row = -1; ++row < 4;) {
                for (int col = -1; ++col < 4;) {
                    if (board.board[row][col] != 0) continue;

                    rowHazard = board.rowHazards[row];
                    colHazard = board.colHazards[col];
                    rowCount = board.rowCounts[row];
                    colCount = board.colCounts[col];

                    // Perform move and check win
                    if (board.move(pieceToPlace, row, col)) {
                        board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                        return -10000 - depth;
                    }
                        // Tie
                    if (board.remainingPieces.size() == 0) {
                        board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                        return 0;
                    }
                    // Iterate over remaining pieces
                    for (byte pieceToOffer: new HashSet<Byte>(board.remainingPieces)) {
                        score = alphabeta(true, board, pieceToOffer, depth-1, alpha, beta);

                        if (score < alpha) {
                            board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                            return score;
                        } else if (score < worstScore) worstScore = score;
                        
                        beta = Math.min(beta, score);
                    }

                    board.undo(row, col, rowHazard, rowCount, colHazard, colCount, prevEqDiagHazard, prevEqDiagCount, prevCompDiagHazard, prevCompDiagCount);
                }
            }

            return worstScore;
        }
    }

    private int heuristic2(QB board) {
        byte metaHazard = 0;

        for (int i = -1; ++i<4;) {
            if (board.rowCounts[i] == 3) {
                metaHazard |= board.rowHazards[i];
            }

            if (board.colCounts[i] == 3) {
                metaHazard |= board.colHazards[i];
            }
        }

        final byte hazard = metaHazard;

        int safe = (int) board.remainingPieces.stream().filter(p -> ((p & hazard) == 0)).count();
        return safe * (((safe & 1) << 1) - 1);
    }

    // Count number of hazards that have an odd number of safe pieces remaining
    // Subtract number of hazards that have an even number of safe pieces remaining
    private int heuristic(QB board) {
        int result = 0;

        for (int i = -1; ++i<4;) {
            int rowCount = countSafePieces(board, board.rowHazards[i]);
            int colCount = countSafePieces(board, board.colHazards[i]);

            result += (((rowCount & 1) << 1) - 1) * board.rowCounts[i];
            result += (((colCount & 1) << 1) - 1) * board.colCounts[i];
        }

        int eqDiagCount = countSafePieces(board, board.eqIdxDiagHazard);
        int compDiagCount = countSafePieces(board, board.compIdxDiagHazard);
        result += (((eqDiagCount & 1) << 1) - 1) * board.eqIdxDiagCount;
        result += ((compDiagCount & 1) << 1) - 1 * board.compIdxDiagCount;

        return result;
    }

    private int countSafePieces(QB board, byte hazard) {
        return (int) board.remainingPieces.stream().filter(p -> ((p & hazard) == 0)).count();
    }

}
