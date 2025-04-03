package com.tictactoe.controller;

import com.tictactoe.model.Game;
import com.tictactoe.model.GameState;
import com.tictactoe.model.Move;
import com.tictactoe.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(
            @RequestParam @NotBlank @Pattern(regexp = "^[XO]$") String symbol,
            @RequestParam @NotBlank @Pattern(regexp = "^(easy|hard)$") String difficultyLevel) {

        Game game = gameService.createGame(symbol, difficultyLevel);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapGameToResponse(game));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<Map<String, Object>> makeMove(
            @PathVariable String gameId,
            @RequestBody @Valid Move move) {

        Game game = gameService.makeMove(gameId, move);
        return ResponseEntity.ok(mapGameToResponse(game));
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<Map<String, Object>> getGameState(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(mapGameToResponse(game));
    }

    private Map<String, Object> mapGameToResponse(Game game) {
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.getId());
        response.put("board", convertBoardToArray(game));
        response.put("currentPlayer", game.getCurrentPlayer().getSymbol());
        response.put("gameState", game.getGameState().toString());
        response.put("winningSequence", game.getWinningSequence());
        response.put("humanSymbol", game.getHumanPlayer().getSymbol());
        response.put("timeRemaining", calculateTimeRemaining(game));

        // If game is over, include a message
        if (game.getGameState() != GameState.IN_PROGRESS) {
            response.put("message", getGameResultMessage(game));
        }

        return response;
    }

    private String[][] convertBoardToArray(Game game) {
        int size = game.getBoard().getSize();
        String[][] boardArray = new String[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                boardArray[row][col] = game.getBoard().getCell(row, col).getValue();
            }
        }

        return boardArray;
    }

    private long calculateTimeRemaining(Game game) {
        LocalDateTime gameEndTime = game.getStartTime().plusMinutes(game.getTimeLimit());
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(gameEndTime)) {
            return 0;
        }

        return java.time.Duration.between(now, gameEndTime).getSeconds();
    }

    private String getGameResultMessage(Game game) {
        switch (game.getGameState()) {
            case X_WON:
                return "Player X won the game!";
            case O_WON:
                return "Player O won the game!";
            case DRAW:
                if (game.isTimeLimitExceeded()) {
                    return "Game ended in a draw due to time limit.";
                } else {
                    return "Game ended in a draw (board is full).";
                }
            default:
                return "Game in progress.";
        }
    }
}