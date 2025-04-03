package com.tictactoe.service.ai;

import com.tictactoe.model.Board;
import com.tictactoe.model.Game;
import com.tictactoe.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HardAIPlayer implements AIPlayer {
    private static final Logger logger = LoggerFactory.getLogger(HardAIPlayer.class);

    @Value("${game.ai.hard.max-depth}")
    private int maxDepth;

    @Value("${game.ai.hard.time-limit-ms}")
    private long timeLimit;

    private String aiSymbol;
    private String humanSymbol;
    private long startTime;
    private Move bestMove;

    @Override
    public Move makeMove(Game game) {
        logger.debug("Hard AI is making a move using minimax with alpha-beta pruning");
        aiSymbol = game.getComputerPlayer().getSymbol();
        humanSymbol = game.getHumanPlayer().getSymbol();
        startTime = System.currentTimeMillis();
        bestMove = null;
        Board board = game.getBoard();

        // For the first move on an empty 20x20 board, play near the center for efficiency
        if (isEmptyBoard(board)) {
            return new Move(board.getSize() / 2, board.getSize() / 2);
        }

        // Use iterative deepening to find the best move within the time limit
        for (int depth = 1; depth <= maxDepth; depth++) {
            if (System.currentTimeMillis() - startTime > timeLimit * 0.8) {
                logger.debug("Time limit approaching, stopping at depth {}", depth - 1);
                break;
            }

            logger.debug("Starting minimax search at depth {}", depth);
            alphabeta(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        }

        logger.debug("Hard AI chose move: ({}, {})", bestMove.getRow(), bestMove.getCol());
        return bestMove;
    }

    private boolean isEmptyBoard(Board board) {
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (!board.getCell(i, j).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private int alphabeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        // Check if time limit is exceeded
        if (System.currentTimeMillis() - startTime > timeLimit) {
            return 0;
        }

        // Check terminal conditions
        if (depth == 0) {
            return evaluateBoard(board);
        }

        List<Move> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return 0; // Draw
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : availableMoves) {
                // Make the move
                board.makeMove(move.getRow(), move.getCol(), aiSymbol);

                // Check if this move would result in a win
                if (board.checkWinningSequence(move.getRow(), move.getCol(), aiSymbol) != null) {
                    // Undo the move
                    board.getCell(move.getRow(), move.getCol()).setValue("");
                    if (depth == maxDepth) {
                        bestMove = move;
                    }
                    return 10000; // Win is highly valued
                }

                // Recursive evaluation
                int eval = alphabeta(board, depth - 1, alpha, beta, false);

                // Undo the move
                board.getCell(move.getRow(), move.getCol()).setValue("");

                if (eval > maxEval) {
                    maxEval = eval;
                    if (depth == maxDepth) {
                        bestMove = move;
                    }
                }

                alpha = Math.max(alpha, maxEval);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : availableMoves) {
                // Make the move
                board.makeMove(move.getRow(), move.getCol(), humanSymbol);

                // Check if this move would result in a loss
                if (board.checkWinningSequence(move.getRow(), move.getCol(), humanSymbol) != null) {
                    // Undo the move
                    board.getCell(move.getRow(), move.getCol()).setValue("");
                    return -10000; // Loss is highly avoided
                }

                // Recursive evaluation
                int eval = alphabeta(board, depth - 1, alpha, beta, true);

                // Undo the move
                board.getCell(move.getRow(), move.getCol()).setValue("");

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, minEval);
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            return minEval;
        }
    }

    private List<Move> getAvailableMoves(Board board) {
        // Optimization: On a 20x20 board, we only consider cells near existing moves
        // This significantly reduces the search space
        List<Move> availableMoves = new ArrayList<>();
        boolean[][] considered = new boolean[board.getSize()][board.getSize()];

        // First find all occupied cells
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (!board.getCell(row, col).isEmpty()) {
                    // Mark all neighbors (within 2 cells) for consideration
                    markNeighborsForConsideration(considered, row, col, board.getSize());
                }
            }
        }

        // Now collect all empty cells that were marked for consideration
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (considered[row][col] && board.getCell(row, col).isEmpty()) {
                    availableMoves.add(new Move(row, col));
                }
            }
        }

        // If no moves found (e.g., first move on empty board), consider center area
        if (availableMoves.isEmpty()) {
            int center = board.getSize() / 2;
            for (int row = center - 2; row <= center + 2; row++) {
                for (int col = center - 2; col <= center + 2; col++) {
                    if (board.isValidPosition(row, col) && board.getCell(row, col).isEmpty()) {
                        availableMoves.add(new Move(row, col));
                    }
                }
            }
        }

        return availableMoves;
    }

    private void markNeighborsForConsideration(boolean[][] considered, int row, int col, int size) {
        for (int i = Math.max(0, row - 2); i <= Math.min(size - 1, row + 2); i++) {
            for (int j = Math.max(0, col - 2); j <= Math.min(size - 1, col + 2); j++) {
                considered[i][j] = true;
            }
        }
    }

    private int evaluateBoard(Board board) {
        // Calculate scores based on potential winning sequences
        int aiScore = calculateScore(board, aiSymbol);
        int humanScore = calculateScore(board, humanSymbol);

        return aiScore - humanScore;
    }

    private int calculateScore(Board board, String symbol) {
        int score = 0;

        // Check horizontal sequences
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize() - 4; col++) {
                score += evaluateSequence(board, row, col, 0, 1, symbol);
            }
        }

        // Check vertical sequences
        for (int row = 0; row < board.getSize() - 4; row++) {
            for (int col = 0; col < board.getSize(); col++) {
                score += evaluateSequence(board, row, col, 1, 0, symbol);
            }
        }

        // Check diagonal sequences (top-left to bottom-right)
        for (int row = 0; row < board.getSize() - 4; row++) {
            for (int col = 0; col < board.getSize() - 4; col++) {
                score += evaluateSequence(board, row, col, 1, 1, symbol);
            }
        }

        // Check diagonal sequences (top-right to bottom-left)
        for (int row = 0; row < board.getSize() - 4; row++) {
            for (int col = 4; col < board.getSize(); col++) {
                score += evaluateSequence(board, row, col, 1, -1, symbol);
            }
        }

        return score;
    }

    private int evaluateSequence(Board board, int row, int col, int rowDir, int colDir, String symbol) {
        String opponentSymbol = symbol.equals(aiSymbol) ? humanSymbol : aiSymbol;
        int ownCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < 5; i++) {
            int newRow = row + i * rowDir;
            int newCol = col + i * colDir;
            String cellValue = board.getCell(newRow, newCol).getValue();

            if (cellValue.equals(symbol)) {
                ownCount++;
            } else if (cellValue.isEmpty()) {
                emptyCount++;
            } else if (cellValue.equals(opponentSymbol)) {
                // If sequence contains opponent's symbol, it's not valuable
                return 0;
            }
        }

        // Score the sequence based on how many of our symbols are in it
        // and if it can still be completed (has enough empty spaces)
        if (ownCount + emptyCount == 5) {
            // Exponential scoring to prefer sequences with more symbols
            return (int) Math.pow(10, ownCount);
        }

        return 0;
    }
}