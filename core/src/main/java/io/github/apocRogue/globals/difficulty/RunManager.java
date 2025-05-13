package io.github.apocRogue.globals.difficulty;

import com.badlogic.gdx.math.MathUtils;

public class RunManager {

    private static final RunManager INSTANCE = new RunManager();

    private int skullLevel = 1;
    private int worldLevel = 1;

    private RunManager() { }


    public static RunManager getInstance() {
        return INSTANCE;
    }

    public int getSkullLevel() {
        return skullLevel;
    }

    public int getWorldLevel() {
        return worldLevel;
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
