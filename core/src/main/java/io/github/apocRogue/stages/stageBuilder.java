package io.github.apocRogue.stages;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.apocRogue.database.DBManager;

public class stageBuilder extends Game {
    @Override
    public void create() {
        try {
            DBManager.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.error("DB", "Failed to open MySQL connection", e);
            Gdx.app.exit();               // or show a popup and stop
            return;
        }
        setScreen(new LoginScreen(this));
    }
}
