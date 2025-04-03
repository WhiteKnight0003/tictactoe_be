package com.tictactoe.service.ai;

import com.tictactoe.model.Board;
import com.tictactoe.model.Game;
import com.tictactoe.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class EasyAIPlayer implements AIPlayer {
    private static final Logger logger = LoggerFactory.getLogger(EasyAIPlayer.class);
    private final Random random = new Random();

    @Override
    public Move makeMove(Game game) {
        logger.debug("Easy AI is making a move");
        Board board = game.getBoard();
        List<Move> availableMoves = new ArrayList<>();

        // Find all available moves
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.getCell(row, col).isEmpty()) {
                    availableMoves.add(new Move(row, col));
                }
            }
        }

        // Choose a random move
        if (!availableMoves.isEmpty()) {
            Move randomMove = availableMoves.get(random.nextInt(availableMoves.size()));
            logger.debug("Easy AI chose move: ({}, {})", randomMove.getRow(), randomMove.getCol());
            return randomMove;
        }

        // This should never happen if the board is not full
        logger.warn("No available moves for Easy AI");
        return null;
    }
}