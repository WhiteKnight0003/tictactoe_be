package main.java.com.tictactoe.model;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Game implements Serializable {
    private String id;
    private Board board;
    private Player humanPlayer;
    private Player computerPlayer;
    private Player currentPlayer;
    private GameState gameState;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime lastMoveTime;
    private int timeLimit;
    private int[] winningSequence; // Stores the winning sequence coordinates if game is won

    public Game(String symbol, String difficultyLevel) {
        this.id = UUID.randomUUID().toString();
        this.board = new Board(20);
        this.humanPlayer = new Player(symbol, "HUMAN");
        this.computerPlayer = new Player(symbol.equals("X") ? "O" : "X", "COMPUTER");
        this.currentPlayer = symbol.equals("X") ? humanPlayer : computerPlayer;
        this.gameState = GameState.IN_PROGRESS;
        this.difficultyLevel = difficultyLevel;
        this.startTime = LocalDateTime.now();
        this.lastMoveTime = this.startTime;
        this.timeLimit = 15; // 15 minutes
        this.winningSequence = null;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }

    public Player getComputerPlayer() {
        return computerPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(LocalDateTime lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int[] getWinningSequence() {
        return winningSequence;
    }

    public void setWinningSequence(int[] winningSequence) {
        this.winningSequence = winningSequence;
    }

    // Helper methods
    public void switchPlayer() {
        this.currentPlayer = (currentPlayer == humanPlayer) ? computerPlayer : humanPlayer;
    }

    public boolean isTimeLimitExceeded() {
        return startTime.plusMinutes(timeLimit).isBefore(LocalDateTime.now());
    }
}