package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class DeathScreen extends ScreenAdapter {

    private Stage stage;
    private Skin skin;
    private stageBuilder game;

    public DeathScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1080, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Gdx.input.setInputProcessor(stage);

        //root table for UI
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        //death label
        Label diedLabel = new Label("YOU DIED!", skin, "title");
        // Optional: you can change style or color
        diedLabel.setFontScale(2f); // Make it bigger

        //retry button
        TextButton retryButton = new TextButton("Retry", skin);
        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to a fresh GameScreen
                game.setScreen(new GameScreen(game));
            }
        });

        //main menu
        TextButton menuButton = new TextButton("Main Menu", skin);
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //back to main screen
                game.setScreen(new MainScreen(game));
            }
        });

        rootTable.defaults().pad(10f);
        rootTable.add(diedLabel).row();
        rootTable.add(retryButton).row();
        rootTable.add(menuButton).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
