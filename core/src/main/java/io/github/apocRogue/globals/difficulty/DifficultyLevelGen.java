package io.github.apocRogue.globals.difficulty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class DifficultyLevelGen {

    public static int getEnemyCount() {
        int diff = CurrentDificulty.getDifficulty();
        int baseEnemies = diff;
        int randomAddition = MathUtils.random(0, diff);
        return baseEnemies + randomAddition;
    }

    public static int getChestCount() {
        int enemyCount = getEnemyCount();
        Gdx.app.log("SPAWN", "EnemyCount = " + enemyCount);
        int chestCount = MathUtils.floor(enemyCount / 2.0f);
        return Math.max(1, chestCount);
    }
}
