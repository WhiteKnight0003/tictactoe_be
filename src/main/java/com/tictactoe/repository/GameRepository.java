package main.java.com.tictactoe.repository;

import com.tictactoe.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class GameRepository {
    private static final String GAME_KEY_PREFIX = "game:";
    private static final long GAME_EXPIRATION_HOURS = 24;

    private final RedisTemplate<String, Game> redisTemplate;

    @Autowired
    public GameRepository(RedisTemplate<String, Game> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Game game) {
        String key = GAME_KEY_PREFIX + game.getId();
        redisTemplate.opsForValue().set(key, game);
        redisTemplate.expire(key, GAME_EXPIRATION_HOURS, TimeUnit.HOURS);
    }

    public Optional<Game> findById(String id) {
        String key = GAME_KEY_PREFIX + id;
        Game game = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(game);
    }

    public void deleteById(String id) {
        redisTemplate.delete(GAME_KEY_PREFIX + id);
    }
}