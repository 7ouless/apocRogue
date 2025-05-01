package io.github.apocRogue.stages;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.apocRogue.database.DBManager;

public class stageBuilder extends Game {
    @Override
    public void create() {
        setScreen(new LoginScreen(this));
    }
}
