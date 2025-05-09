package io.github.apocRogue.stages;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.map.GrassOverlayTile;
import io.github.apocRogue.weapons.Weapon;


import io.github.apocRogue.globals.physics.SoundPhysics;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {

    private stageBuilder game;
    private Stage stage;       // For gameplay
    private Stage uiStage;     // For HUD / normal UI
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private float cameraOffsetY = 160f;


    private Stage overlayStage;
    private Texture bgSky, bgMountains, bgSun, bgSideClouds, bgTopClouds, bgSmallClouds;
    private float viewportWidth, viewportHeight;

    private GameWorld gameWorld;
    private boolean paused = false; // Tracks if the game is paused

    // Pause overlay members
    private Table pauseOverlay;    // We'll add this table to uiStage and toggle visibility
    private Skin skin;             // We assume you load a Skin for UI
    private ShapeRenderer shapeRenderer;

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

        stage = new Stage(new FitViewport(1920, 1080, camera));
        uiStage = new Stage(new FitViewport(1920, 1080));

        overlayStage = new Stage(new FitViewport(1920, 1080, camera));


        viewportWidth  = stage.getViewport().getWorldWidth();
        viewportHeight = stage.getViewport().getWorldHeight();


        bgSky       = new Texture(Gdx.files.internal("ui/sky.png"));
        bgMountains = new Texture(Gdx.files.internal("ui/mountains.png"));
        bgSun          = new Texture(Gdx.files.internal("ui/sun.png"));
        bgSideClouds   = new Texture(Gdx.files.internal("ui/side-clouds.png"));
        bgTopClouds    = new Texture(Gdx.files.internal("ui/top-clouds.png"));
        bgSmallClouds  = new Texture(Gdx.files.internal("ui/small-clouds.png"));

        batch = new SpriteBatch();

        // Load a skin for UI. If you already do this in GameWorld, that is fine
        // but typically the game screen or the UI system loads the skin:
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create the game logic container
        gameWorld = new GameWorld(stage);
        gameWorld.initialize();
        gameWorld.getInventory().draw(uiStage);

        // ─── move *all* grass into the overlay stage ───
        List<Actor> grassActors = new ArrayList<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof GrassOverlayTile) grassActors.add(a);
        }
        for (Actor grass : grassActors) {
            grass.remove();
            overlayStage.addActor(grass);
        }

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
        // 1) Update logic & stages
        if (!paused) {
            gameWorld.update(delta);
            stage.act(delta);
            overlayStage.act(delta);
        }
        gameWorld.getInventory().draw(uiStage);

// 2) Center camera on player, but lift it up by cameraOffsetY
        float playerCenterX = gameWorld.getPlayer().getX() + gameWorld.getPlayer().getWidth()  / 2f;
        float playerCenterY = gameWorld.getPlayer().getY() + gameWorld.getPlayer().getHeight() / 2f;

        camera.position.set(
            playerCenterX,
            playerCenterY + cameraOffsetY,
            0f
        );
        camera.update();

        stage.getViewport().apply();

// 3) Clear screen
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// 4) Parallax pass
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float left   = camera.position.x - viewportWidth  / 2f;
        float bottom = camera.position.y - viewportHeight / 2f;

        // 4.1) Sky (stationary relative to camera)
        batch.draw(bgSky, left, bottom, viewportWidth, viewportHeight);

        // 4.2) Sun (almost stationary; slight horizontal drift if you like)
        float sunScale = 0.5f; // adjust size
        float sunW = bgSun.getWidth() * sunScale;
        float sunH = bgSun.getHeight() * sunScale;
        // Position it at, say, 20% from left, 70% from bottom of the viewport
        batch.draw(
            bgSun,
                    left + viewportWidth * 0.2f - sunW/2,
                    bottom + viewportHeight * 0.7f - sunH/2,
                    sunW,
                    sunH
                );

        // 4.3) Side‐clouds (very slow parallax)
                drawTiledLayer(
                        batch,
                        bgSideClouds,
                        left,
                       bottom + viewportHeight * 0f,
                       -0.02f,   // very subtle scroll
                        0.4f      // scale clouds to 80%
                        );

        // 4.4) Mountains (slow scroll)
        drawTiledLayer(batch,
            bgMountains,
            left,
            bottom + 40f,   // vertical offset
            -0.055f,         // parallax factor
            0.5f);          // scale

        // 4.5) Top‐clouds (medium parallax)
               drawTiledLayer(
                   batch,
                   bgTopClouds,
                   left,
                   bottom + viewportHeight * 0.8f,
                   -0.08f,
                   0.6f
               );

        // 4.6) Small‐clouds (faster parallax, smaller)
               drawTiledLayer(
                   batch,
                   bgSmallClouds,
                   left,
                   bottom + viewportHeight * 0.4f,
                   -0.12f,
                   0f
               );

        // …and more layers here…
        batch.end();

        // 5) Draw the rest of the world
        stage.draw();

        // 6) Any overlay Stage (e.g. grass)
        overlayStage.setViewport(stage.getViewport());
        overlayStage.draw();

        // 7) Your debug‐shape passes
        shapeRenderer.setProjectionMatrix(camera.combined);

        // filled‐shape pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //   ← draw your filled debug shapes here, e.g.:
        //   shapeRenderer.circle(enemyX, enemyY, radius);
        shapeRenderer.end();

        // line‐shape pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (SoundPhysics.SoundDebugEvent evt : SoundPhysics.debugEvents) {
            float t     = evt.timeAlive / evt.duration;
            float alpha = 1f - t;
            shapeRenderer.setColor(evt.color.r, evt.color.g, evt.color.b, alpha);
            shapeRenderer.circle(evt.center.x, evt.center.y, evt.currentRadius);
        }
        shapeRenderer.end();


        // 8) Simple dark-pink full-screen tint
        Gdx.gl.glEnable(GL20.GL_BLEND);  // turn on alpha blending
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // RGBA = (red=0.6, green=0.0, blue=0.2, alpha=0.1) → dark pink at 10% opacity
        shapeRenderer.setColor(0.4f, 0.0f, 0.2f, 0.12f);
        // compute the bottom-left corner of the camera’s view in world coords
        float tintX = camera.position.x - viewportWidth  / 2f;
        float tintY = camera.position.y - viewportHeight / 2f;
        // draw a rectangle that exactly covers the screen
        shapeRenderer.rect(tintX, tintY, viewportWidth, viewportHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);


        // 9) Finally the UI
        uiStage.act(delta);
        uiStage.draw();

    }

    private void drawTiledLayer(SpriteBatch batch,
                                Texture tex,
                                float worldLeft,
                                float y,
                                float parallaxFactor,
                                float scale) {
        float tileW = tex.getWidth()  * scale;
        float scroll = (camera.position.x * parallaxFactor) % tileW;
        if (scroll > 0) scroll -= tileW;

        for (float x = worldLeft + scroll - tileW;
             x < worldLeft + viewportWidth;
             x += tileW) {
            batch.draw(tex, x, y, tileW, tex.getHeight() * scale);
        }
    }


    private PlayerActor findPlayerInStage(Stage stage) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof PlayerActor) {
                return (PlayerActor) actor;
            }
        }
        return null;
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
        shapeRenderer.dispose();
        overlayStage.dispose();
        bgSky.dispose();
        bgMountains.dispose();
        bgSun.dispose();
        bgSideClouds.dispose();
        bgTopClouds.dispose();
        bgSmallClouds.dispose();

    }
}
