package io.github.apocRogue.globals.difficulty;

import com.badlogic.gdx.math.MathUtils;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;

public class RunManager {
    private int skullLevel = 1;
    public int worldLevel = 1;


    public RunManager() {}

    public static RunManager getInstance() {
        return null;
    }

    public int getSkullLevel() {
        return skullLevel;
    }

    public int getWorldLevel() {
        return worldLevel;
    }

    public void setSkullLevel(int level) {
        this.skullLevel = level;
    }

    public void setWorldLevel(int worldLevel) {
        this.worldLevel = worldLevel;
    }

    public void advanceWorld() {
        worldLevel++;
    }


    public void continueRun() {
        skullLevel += MathUtils.random(1, 3);
        worldLevel = 1;
    }


    public boolean isFinalWorld() {
        return worldLevel == 5;
    }
}
