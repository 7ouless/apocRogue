package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.ScreenUtils;

public class OptionsScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;
    private GameScreen previousGameScreen; // store the paused GameScreen

    public OptionsScreen(stageBuilder game, GameScreen previousGameScreen) {
        this.game = game;
        this.previousGameScreen = previousGameScreen;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton controlsButton = new TextButton("Controls", skin);
        TextButton audioButton = new TextButton("Audio", skin);
        TextButton backButton = new TextButton("Back", skin);

        controlsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Open the Controls screen.
                game.setScreen(new ControlsScreen(game, previousGameScreen));
            }
        });

        audioButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Open the Audio screen.
                game.setScreen(new AudioScreen(game, previousGameScreen));
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to the previous GameScreen.
                game.setScreen(new EscScreen(game, previousGameScreen));
            }
        });

        table.add(controlsButton).pad(10).row();
        table.add(audioButton).pad(10).row();
        table.add(backButton).pad(10).row();
    }
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
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
