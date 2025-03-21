package io.github.apocRogue.stages;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.inventory.Inventory;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.stages.GameWorld; // <-- Our new logic class


public class GameScreen extends ScreenAdapter {

    private stageBuilder game;
    private Stage stage;       // For gameplay
    private Stage uiStage;     // For HUD / normal UI
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private GameWorld gameWorld;
    private boolean paused = false; // Tracks if the game is paused

    // Pause overlay members
    private Table pauseOverlay;    // We'll add this table to uiStage and toggle visibility
    private Skin skin;             // We assume you load a Skin for UI

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

        stage = new Stage(new FitViewport(1920, 1080, camera));
        uiStage = new Stage(new FitViewport(1920, 1080));

        batch = new SpriteBatch();

        // Load a skin for UI. If you already do this in GameWorld, that is fine
        // but typically the game screen or the UI system loads the skin:
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create the game logic container
        gameWorld = new GameWorld(stage);
        gameWorld.initialize();
        gameWorld.getInventory().draw(uiStage);


        // Create normal UI or HUD elements here (if any)...

        // Create the pause overlay but keep it hidden initially
        createPauseOverlay();

        // Set up input
        InputMultiplexer multiplexer = new InputMultiplexer(uiStage, stage);

        // Optionally, you can also add a listener for ESC from here if you prefer:
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePause();
                }
                return false;
            }
        });
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.TAB) {
                    gameWorld.getInventory().toggleInventory();
                }
                return false;
            }
        });
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // If user scrolls up/down, call inventory.scrollHotbar
                // “amountY” is positive or negative depending on scroll direction
                gameWorld.getInventory().scrollHotbar((int) amountY);
                return false;
            }
        });
        uiStage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Weapon w = gameWorld.getInventory().getSelectedWeapon();
                    if (w != null) {
                        w.use(gameWorld.getPlayer(), stage);
                    }
                    // Return true if you want to consume the event
                    return true;
                }
                return false;
            }
        });

// Then set the multiplexer
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void createPauseOverlay() {
        // This Table covers the entire screen and darkens the background
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);

        // A semi-transparent background (dark overlay)
        pauseOverlay.setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));

        // Add the table to the uiStage
        uiStage.addActor(pauseOverlay);

        // Now create an inner table to hold the actual menu buttons
        Table menuTable = new Table();
        // For convenience, we center it
        menuTable.center();
        pauseOverlay.add(menuTable);

        // Example Buttons: Resume, Options, Quit
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton optionsButton = new TextButton("Options", skin);
        TextButton exitButton = new TextButton("Exit to Main Menu", skin);

        // Add them all to menuTable
        menuTable.row();
        menuTable.add(resumeButton).pad(10);
        menuTable.row();
        menuTable.add(optionsButton).pad(10);
        menuTable.row();
        menuTable.add(exitButton).pad(10);

        // Add click listeners
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause(); // unpause
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // Hide the pause overlay so it no longer shows
                pauseOverlay.setVisible(false);

                OptionsOverlay myOverlay = new OptionsOverlay(skin);
                myOverlay.setFillParent(true);
                myOverlay.setModal(true);
                myOverlay.setBackground(skin.newDrawable("white", 0, 0, 0, 0.8f));

// Optionally center your internal content
                Table content = new Table();
                content.center();
                myOverlay.add(content).expand().fill();

// Then in 'content', add your "Controls" and "Audio" buttons.

                myOverlay.setOptionsListener(new OptionsOverlay.OptionsListener() {
                    @Override
                    public void onShowControls() {
                        // remove the OptionsOverlay and show the ControlsOverlay
                        myOverlay.remove();
                        uiStage.addActor(new ControlsOverlay(skin, myOverlay));
                    }
                    @Override
                    public void onShowAudio() {
                        myOverlay.remove();
                        uiStage.addActor(new AudioOverlay(skin, myOverlay));
                    }
                    @Override
                    public void onCloseOptions() {
                        // remove the OptionsOverlay
                        myOverlay.remove();
                        // (Optional) If you want to go *back* to the pause overlay, re-show it:
                        pauseOverlay.setVisible(true);
                    }
                });
                uiStage.addActor(myOverlay);

// 4) Now it has a Stage, so we can safely center it
// Now center it on the UI stage
                float stageWidth = uiStage.getViewport().getWorldWidth();
                float stageHeight = uiStage.getViewport().getWorldHeight();
                myOverlay.setPosition((stageWidth - myOverlay.getWidth()) / 2f,
                    (stageHeight - myOverlay.getHeight()) / 2f);
            }
        });



        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Return to main menu or wherever you want
                game.setScreen(new MainScreen(game));
            }
        });

        // Hide it by default
        pauseOverlay.setVisible(false);
    }

    private void togglePause() {
        paused = !paused;
        pauseOverlay.setVisible(paused);
    }

    @Override
    public void render(float delta) {
        // If not paused, update the game logic
        if (!paused) {
            gameWorld.update(delta);
        }
        gameWorld.getInventory().draw(uiStage);

        // Then, regardless of paused or not, do camera stuff
        camera.position.set(
            gameWorld.getPlayer().getX() + gameWorld.getPlayer().getWidth() / 2f,
            gameWorld.getPlayer().getY() + gameWorld.getPlayer().getHeight() / 2f,
            0
        );
        camera.update();
        stage.getViewport().apply();
        batch.setProjectionMatrix(camera.combined);

        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the main stage (game)
        stage.draw();

        // Update & render the UI stage (includes the pause overlay)
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        uiStage.getViewport().update(width, height);
    }

    @Override
    public void dispose() {
        gameWorld.dispose();
        stage.dispose();
        uiStage.dispose();
        skin.dispose();
        batch.dispose();
    }
}
