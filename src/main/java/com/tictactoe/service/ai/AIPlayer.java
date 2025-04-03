package com.tictactoe.service.ai;

import com.tictactoe.model.Game;
import com.tictactoe.model.Move;

public interface AIPlayer {
    /**
     * Makes a move for the AI player.
     *
     * @param game the game
     * @return the move made
     */
    Move makeMove(Game game);
}