package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EscScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game; // Reference to your main game class
    private GameScreen gameScreen; // Store the paused game screen

    public EscScreen(stageBuilder game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen; // store the reference
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create a full-screen table that acts as a dark overlay.
        Table overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(skin.newDrawable("white", 0, 0, 0, 0.8f));
        stage.addActor(overlayTable);

        // Create a table to hold the buttons.
        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        stage.addActor(buttonTable);

        // Create the buttons.
        TextButton continueButton = new TextButton("Continue", skin);
        TextButton optionsButton = new TextButton("Options", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // Set up button listeners.
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(gameScreen);
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Open the options/settings screen.
                game.setScreen(new OptionsScreen(game, gameScreen));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Return to the main menu.
                game.setScreen(new MainScreen(game));
            }
        });

        // Arrange the buttons in a column.
        buttonTable.add(continueButton).pad(10);
        buttonTable.row();
        buttonTable.add(optionsButton).pad(10);
        buttonTable.row();
        buttonTable.add(exitButton).pad(10);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        // Optionally: allow pressing Escape again to resume.
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
        }
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
