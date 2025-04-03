package com.tictactoe.model;


import java.io.Serializable;

public class Board implements Serializable {
    private final int size;
    private final Cell[][] cells;

    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public int getSize() {
        return size;
    }

    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return cells[row][col];
        }
        return null;
    }

    public boolean makeMove(int row, int col, String symbol) {
        if (isValidPosition(row, col) && cells[row][col].isEmpty()) {
            cells[row][col].setValue(symbol);
            return true;
        }
        return false;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public boolean isFull() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (cells[row][col].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    // Check for 5 in a row (horizontal, vertical, or diagonal)
    public int[] checkWinningSequence(int row, int col, String symbol) {
        // Check horizontal
        int[] sequence = checkDirection(row, col, 0, 1, symbol);
        if (sequence != null) return sequence;

        // Check vertical
        sequence = checkDirection(row, col, 1, 0, symbol);
        if (sequence != null) return sequence;

        // Check diagonal (top-left to bottom-right)
        sequence = checkDirection(row, col, 1, 1, symbol);
        if (sequence != null) return sequence;

        // Check diagonal (top-right to bottom-left)
        sequence = checkDirection(row, col, 1, -1, symbol);
        if (sequence != null) return sequence;

        return null;
    }

    private int[] checkDirection(int row, int col, int rowDir, int colDir, String symbol) {
        int count = 1;
        int[] sequence = new int[10]; // Store the winning sequence coordinates (5 pairs)
        sequence[0] = row;
        sequence[1] = col;

        // Check forward
        for (int i = 1; i < 5; i++) {
            int newRow = row + i * rowDir;
            int newCol = col + i * colDir;
            if (!isValidPosition(newRow, newCol) || !cells[newRow][newCol].getValue().equals(symbol)) {
                break;
            }
            count++;
            sequence[i*2] = newRow;
            sequence[i*2+1] = newCol;
        }

        // Check backward
        for (int i = 1; i < 5; i++) {
            int newRow = row - i * rowDir;
            int newCol = col - i * colDir;
            if (!isValidPosition(newRow, newCol) || !cells[newRow][newCol].getValue().equals(symbol)) {
                break;
            }
            count++;
            // Shift existing coordinates to make room for backward checks
            for (int j = 8; j >= 0; j -= 2) {
                sequence[j+2] = sequence[j];
                sequence[j+3] = sequence[j+1];
            }
            sequence[0] = newRow;
            sequence[1] = newCol;
        }

        return count >= 5 ? sequence : null;
    }

    // Clone the board for AI evaluation
    public Board clone() {
        Board clonedBoard = new Board(this.size);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                clonedBoard.cells[row][col].setValue(this.cells[row][col].getValue());
            }
        }
        return clonedBoard;
    }
}