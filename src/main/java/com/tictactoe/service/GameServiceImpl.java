package com.tictactoe.service;

import com.tictactoe.exception.GameNotFoundException;
import com.tictactoe.exception.InvalidMoveException;
import com.tictactoe.model.Game;
import com.tictactoe.model.GameState;
import com.tictactoe.model.Move;
import com.tictactoe.repository.GameRepository;
import com.tictactoe.service.ai.AIPlayer;
import com.tictactoe.service.ai.AIPlayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameRepository gameRepository;
    private final AIPlayerFactory aiPlayerFactory;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, AIPlayerFactory aiPlayerFactory) {
        this.gameRepository = gameRepository;
        this.aiPlayerFactory = aiPlayerFactory;
    }

    @Override
    public Game createGame(String symbol, String difficultyLevel) {
        logger.info("Creating new game with symbol: {} and difficulty: {}", symbol, difficultyLevel);

        if (!symbol.equals("X") && !symbol.equals("O")) {
            throw new IllegalArgumentException("Symbol must be either X or O");
        }

        if (!difficultyLevel.equals("easy") && !difficultyLevel.equals("hard")) {
            throw new IllegalArgumentException("Difficulty level must be either easy or hard");
        }

        Game game = new Game(symbol, difficultyLevel);
        gameRepository.save(game);

        // If computer goes first, make an AI move
        if (game.getCurrentPlayer().getSymbol().equals(game.getComputerPlayer().getSymbol())) {
            return makeAIMove(game);
        }

        return game;
    }

    @Override
    public Game makeMove(String gameId, Move move) {
        logger.info("Making move for game: {} at position: ({}, {})", gameId, move.getRow(), move.getCol());
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));

        // Check if game is already over
        if (game.getGameState() != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is already over");
        }

        // Check for time limit
        if (checkTimeLimit(game)) {
            game.setGameState(GameState.DRAW);
            gameRepository.save(game);
            return game;
        }

        // Validate move
        if (!game.getBoard().isValidPosition(move.getRow(), move.getCol())) {
            throw new InvalidMoveException("Invalid position: (" + move.getRow() + ", " + move.getCol() + ")");
        }

        if (!game.getBoard().getCell(move.getRow(), move.getCol()).isEmpty()) {
            throw new InvalidMoveException("Cell already occupied at: (" + move.getRow() + ", " + move.getCol() + ")");
        }

        // Make the move
        game.getBoard().makeMove(move.getRow(), move.getCol(), game.getCurrentPlayer().getSymbol());
        game.setLastMoveTime(LocalDateTime.now());

        // Update game state
        updateGameState(game, move.getRow(), move.getCol());

        // If game is still in progress, switch player and make AI move if it's computer's turn
        if (game.getGameState() == GameState.IN_PROGRESS) {
            game.switchPlayer();
            gameRepository.save(game);

            if (!game.getCurrentPlayer().isHuman()) {
                return makeAIMove(game);
            }
        } else {
            gameRepository.save(game);
        }

        return game;
    }

    @Override
    public Game getGame(String gameId) {
        logger.info("Retrieving game with ID: {}", gameId);
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
    }

    @Override
    public void updateGameState(Game game, int row, int col) {
        // Check for win
        int[] winningSequence = game.getBoard().checkWinningSequence(row, col, game.getCurrentPlayer().getSymbol());
        if (winningSequence != null) {
            game.setWinningSequence(winningSequence);
            if (game.getCurrentPlayer().getSymbol().equals("X")) {
                game.setGameState(GameState.X_WON);
            } else {
                game.setGameState(GameState.O_WON);
            }
            logger.info("Player {} won the game", game.getCurrentPlayer().getSymbol());
            return;
        }

        // Check for draw (full board)
        if (game.getBoard().isFull()) {
            game.setGameState(GameState.DRAW);
            logger.info("Game ended in a draw (full board)");
            return;
        }

        // Check for time limit
        if (checkTimeLimit(game)) {
            game.setGameState(GameState.DRAW);
            logger.info("Game ended in a draw (time limit exceeded)");
        }
    }

    @Override
    public Game makeAIMove(Game game) {
        logger.info("Making AI move for game: {} with difficulty: {}", game.getId(), game.getDifficultyLevel());

        // Check if game is already over
        if (game.getGameState() != GameState.IN_PROGRESS) {
            return game;
        }

        AIPlayer aiPlayer = aiPlayerFactory.createAIPlayer(game.getDifficultyLevel());
        Move aiMove = aiPlayer.makeMove(game);

        // Make the move
        game.getBoard().makeMove(aiMove.getRow(), aiMove.getCol(), game.getCurrentPlayer().getSymbol());
        game.setLastMoveTime(LocalDateTime.now());

        // Update game state
        updateGameState(game, aiMove.getRow(), aiMove.getCol());

        // If game is still in progress, switch player
        if (game.getGameState() == GameState.IN_PROGRESS) {
            game.switchPlayer();
        }

        gameRepository.save(game);
        return game;
    }

    @Override
    public boolean checkTimeLimit(Game game) {
        return game.isTimeLimitExceeded();
    }
}