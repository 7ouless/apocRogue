package io.github.apocRogue.stages;

import com.badlogic.gdx.Game;
import io.github.apocRogue.actors.mapEntities.Door;
import io.github.apocRogue.database.DBManager;

public class stageBuilder extends Game {
    @Override
    public void create() {
        setScreen(new MainScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();        // disposes current Screen (e.g. GameScreen, MainScreen)
        Door.disposeTextures(); // dispose shared door textures once, on app exit
    }
}
