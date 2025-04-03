package com.tictactoe.model;

import java.io.Serializable;

public class Player implements Serializable {
    private final String symbol;
    private final String type;

    public Player(String symbol, String type) {
        this.symbol = symbol;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    public boolean isHuman() {
        return "HUMAN".equals(type);
    }
}