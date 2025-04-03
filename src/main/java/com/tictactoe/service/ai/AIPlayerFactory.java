package com.tictactoe.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIPlayerFactory {
    private final EasyAIPlayer easyAIPlayer;
    private final HardAIPlayer hardAIPlayer;

    @Autowired
    public AIPlayerFactory(EasyAIPlayer easyAIPlayer, HardAIPlayer hardAIPlayer) {
        this.easyAIPlayer = easyAIPlayer;
        this.hardAIPlayer = hardAIPlayer;
    }

    public AIPlayer createAIPlayer(String difficultyLevel) {
        if ("hard".equalsIgnoreCase(difficultyLevel)) {
            return hardAIPlayer;
        } else {
            return easyAIPlayer;
        }
    }
}