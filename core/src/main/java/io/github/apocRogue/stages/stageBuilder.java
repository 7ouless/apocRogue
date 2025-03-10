package io.github.apocRogue.stages;

import com.badlogic.gdx.Game;

public class stageBuilder extends Game {
    @Override
    public void create() {
        // Initialize resources common to all screens if needed
        setScreen(new MainScreen(this));
    }
}
