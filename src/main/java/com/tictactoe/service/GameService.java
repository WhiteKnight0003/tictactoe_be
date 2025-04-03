package com.tictactoe.service;


import com.tictactoe.model.Game;
import com.tictactoe.model.GameState;
import com.tictactoe.model.Move;

public interface GameService {
    /**
     * Creates a new game with the given player symbol and difficulty level.
     *
     * @param symbol          the symbol chosen by the human player (X or O)
     * @param difficultyLevel the difficulty level (easy or hard)
     * @return the created game
     */
    Game createGame(String symbol, String difficultyLevel);

    /**
     * Makes a move for the human player.
     *
     * @param gameId the ID of the game
     * @param move   the move to make
     * @return the updated game
     */
    Game makeMove(String gameId, Move move);

    /**
     * Gets the current state of the game.
     *
     * @param gameId the ID of the game
     * @return the game
     */
    Game getGame(String gameId);

    /**
     * Updates the game state based on the last move made.
     *
     * @param game the game to update
     * @param row  the row of the last move
     * @param col  the column of the last move
     */
    void updateGameState(Game game, int row, int col);

    /**
     * Makes a move for the AI player.
     *
     * @param game the game
     * @return the updated game
     */
    Game makeAIMove(Game game);

    /**
     * Checks if the game has ended due to time limit.
     *
     * @param game the game to check
     * @return true if the time limit has been exceeded, false otherwise
     */
    boolean checkTimeLimit(Game game);
}