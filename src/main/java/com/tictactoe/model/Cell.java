package main.java.com.tictactoe.model;


import java.io.Serializable;

public class Cell implements Serializable {
    private final int row;
    private final int col;
    private String value;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.value = "";
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }
}