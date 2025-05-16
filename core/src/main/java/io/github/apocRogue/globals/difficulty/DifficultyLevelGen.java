package io.github.apocRogue.globals.difficulty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class DifficultyLevelGen {

    public static int getEnemyCount() {
        int diff = CurrentDificulty.getDifficulty();
        int baseEnemies = diff / 4;
        int randomAddition = MathUtils.random(0, diff / 2);
        return baseEnemies + randomAddition;
    }

    public static int getChestCount() {
        int enemyCount = getEnemyCount();
        Gdx.app.log("SPAWN", "EnemyCount = " + enemyCount);
        int chestCount = MathUtils.floor(enemyCount );
        return Math.max(1, chestCount);
    }
}
