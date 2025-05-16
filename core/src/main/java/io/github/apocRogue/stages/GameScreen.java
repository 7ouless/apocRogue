package io.github.apocRogue.stages;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
import io.github.apocRogue.globals.difficulty.CurrentDificulty;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.inventory.menuinventory.InventoryService;
import io.github.apocRogue.map.*;
import io.github.apocRogue.weapons.Weapon;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import io.github.apocRogue.globals.difficulty.RunManager;
import io.github.apocRogue.actors.mapEntities.Door;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import io.github.apocRogue.stages.DeathScreen;

import io.github.apocRogue.globals.physics.SoundPhysics;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {

    private Music musicRad2;
    private Music musicRad3;
    private Music bossMusic;
    private Music currentMusic;

    private stageBuilder game;
    private Stage stage;       // For gameplay
    private Stage uiStage;     // For HUD / normal UI
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private ProgressBar staminaBar;
    private ProgressBar healthBar;

    private final RunManager runMgr = RunManager.getInstance();
    private Label skullLabel, worldLabel;
    private GameWorld gameWorld;

    private float cameraOffsetY = 160f;
    private float initialCamY;
    private float maxCameraYOffset = 200f;
    private float cameraSmoothFactor = 5f;

    private float minCameraX, maxCameraX;

    private Stage overlayStage;
    private Texture bgSky, bgMountains, bgSun, bgSideClouds, bgTopClouds, bgSmallClouds, bgBigTree,bgMeadow;
    private float viewportWidth, viewportHeight;
    private Texture grainTex;

    private boolean paused = false; // Tracks if the game is paused
    private Inventory inventory;

    // Pause overlay members
    private Table pauseOverlay;
    private Skin skin;
    private ShapeRenderer shapeRenderer;

    public GameScreen(stageBuilder game) {
        this.inventory = new Inventory(skin);

        this.game = game;
    }

    private void loadBackgrounds(String folder){
        //dispose old BG textures
        if (bgSky != null) bgSky.dispose();
        if (bgMountains != null) bgMountains.dispose();
        if (bgSun != null) bgSun.dispose();
        if (bgSideClouds != null) bgSideClouds.dispose();
        if (bgTopClouds != null) bgTopClouds.dispose();
        if (bgSmallClouds != null) bgSmallClouds.dispose();
        if (bgBigTree != null) bgBigTree.dispose();
        if (bgMeadow != null) bgMeadow.dispose();

        // load the new ones
        bgSky        = new Texture(Gdx.files.internal("ui/"+folder+"/sky.png"));
        bgMountains  = new Texture(Gdx.files.internal("ui/"+folder+"/mountains.png"));
        bgSun        = new Texture(Gdx.files.internal("ui/"+folder+"/sun.png"));
        bgSideClouds = new Texture(Gdx.files.internal("ui/"+folder+"/side-clouds.png"));
        bgTopClouds  = new Texture(Gdx.files.internal("ui/"+folder+"/top-clouds.png"));
        bgSmallClouds= new Texture(Gdx.files.internal("ui/"+folder+"/small-clouds.png"));

        String bigTreeAsset = "/big-tree.png";
        if ("high".equals(folder)) {                           // radiation 3
            bigTreeAsset = "/big-tree2.png";
        }
        bgBigTree    = new Texture(Gdx.files.internal("ui/"+folder+ bigTreeAsset));
        bgMeadow     = new Texture(Gdx.files.internal("ui/"+folder+"/meadow.png"));
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);
        camera.update();
        initialCamY = camera.position.y;

        stage = new Stage(new FitViewport(1920, 1080, camera));
        uiStage = new Stage(new FitViewport(1920, 1080));

        overlayStage = new Stage(new FitViewport(1920, 1080, camera));


        viewportWidth  = stage.getViewport().getWorldWidth();
        viewportHeight = stage.getViewport().getWorldHeight();

        float halfVW = viewportWidth * 0.5f;
        minCameraX = halfVW;
        maxCameraX = MapManager.settings.roomWidth - halfVW;

        // load the correct BGs for current radiation level
        int rad = CurrentDificulty.getRadiation();
        String folder = (rad == 2 ? "med" : rad == 3 ? "high" : "low");
        loadBackgrounds(folder);
        TreeTile.loadForRadiation(folder);
        GrassOverlayTile.loadForRadiation(folder);
        DecorTile.loadForRadiation(folder);
        PlatformGrassOverlayTile.loadForRadiation(folder);

        // load your two looping tracks
        musicRad2 = Gdx.audio.newMusic(Gdx.files.internal("audio/radiation2.mp3"));
        musicRad3 = Gdx.audio.newMusic(Gdx.files.internal("audio/radiation3.mp3"));
        bossMusic  = Gdx.audio.newMusic(Gdx.files.internal("audio/boss-music.mp3"));
        // helper to pick & play
        switchMusic(rad);


        batch = new SpriteBatch();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // 1) Init our run HUD
        loadCurrentWorld();
        initUI();


        // move all decor into the overlay stage
        List<Actor> decorActors = new ArrayList<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof DecorTile) {
                decorActors.add(a);
            }
        }
        for (Actor d : decorActors) {
            d.remove();              // detach from main stage
            overlayStage.addActor(d);
        }

        // move all grass into the overlay stage
        List<Actor> grassActors = new ArrayList<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof GrassOverlayTile
              ) {
                grassActors.add(a);
            }
        }
        for (Actor g : grassActors) {
            g.remove();
            overlayStage.addActor(g);
        }

        // Create normal UI or HUD elements here (if any)...

        // Create the pause overlay but keep it hidden initially
        createPauseOverlay();

        // Set up input
        InputMultiplexer multiplexer = new InputMultiplexer(uiStage, stage);


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
                    PlayerActor p = gameWorld.getPlayer();
                    Weapon w       = gameWorld.getInventory().getSelectedWeapon();
                    p.startAttack();             // flip to attack sprite
                    if (p.tryAttack()) {
                        if (w != null) w.use(p, stage);
                        return true;
                    }
                }
                return false;
            }
        });

// Then set the multiplexer
        Gdx.input.setInputProcessor(multiplexer);

        //Grain
        if (grainTex != null) grainTex.dispose();
        Pixmap pix = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                float a = MathUtils.random() * 0.2f;
                pix.setColor(1f, 1f, 1f, a);
                pix.drawPixel(x, y);
            }
        }
        grainTex = new Texture(pix);
        grainTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pix.dispose();

    }

    private void loadCurrentWorld() {
        if (gameWorld != null) {
            gameWorld.dispose();
        }

        int diff = runMgr.getSkullLevel() * 5 + runMgr.getWorldLevel();
        CurrentDificulty.setDifficulty(diff);

        gameWorld = new GameWorld(stage, runMgr.isFinalWorld(), inventory);
        gameWorld.initialize();

        Actor[] actors = stage.getActors().toArray(Actor.class);
        for (Actor a : actors) {
            if (a instanceof DecorTile || a instanceof GrassOverlayTile) {
                a.remove();
                overlayStage.addActor(a);
            }
        }

    }

    private void switchMusic(int rad) {
        // stop whatever’s playing
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }

        // rad == 1 or 2 --> play musicRad2, rad == 3 --> musicRad3, if world level5 --> boss-music
        if  (runMgr.isFinalWorld()) {
            currentMusic = bossMusic;
        } else if (rad >= 1 && rad <= 2) {
            currentMusic = musicRad2;
        } else if (rad == 3) {
            currentMusic = musicRad3;
        } else {
            currentMusic = null;
        }


        if (currentMusic != null) {
            currentMusic.setLooping(true);
            currentMusic.setVolume(0.05f);
            currentMusic.play();
        }
    }



    private void initUI() {
        // 1) Root HUD table, anchored to the top
        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().padTop(20);
        uiStage.addActor(hud);

        // 2) Clone the skin’s real default-horizontal style
        ProgressBar.ProgressBarStyle baseStyle =
            skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class);
        ProgressBar.ProgressBarStyle staminaStyle =
            new ProgressBar.ProgressBarStyle(baseStyle);

        // 3) Recolor just the filled part (knobBefore) to yellow
        staminaStyle.knobBefore =
            skin.newDrawable("progress-bar-square-knob", Color.YELLOW);

        // 4) Keep the empty track dark grey
        staminaStyle.background =
            skin.newDrawable("progress-bar-square", Color.DARK_GRAY);

        // 5) Force the drawables to a taller height (8 px)
        staminaStyle.background .setMinHeight(8f);
        staminaStyle.knobBefore  .setMinHeight(8f);

        // 6) Create your difficulty labels
        skullLabel = new Label("Skull: " + runMgr.getSkullLevel(), skin);
        skullLabel.setFontScale(1.5f);
        worldLabel = new Label("World: " + runMgr.getWorldLevel(), skin);
        worldLabel.setFontScale(1.2f);

        // 7) Instantiate the stamina and health bar with the custom style
        staminaBar = new ProgressBar(0, 100, 1, false, staminaStyle);
        staminaBar.setValue(gameWorld.getPlayer().getStats().getStamina());
        staminaBar.setAnimateDuration(0.1f);

        int maxHP = gameWorld.getPlayer().getStats().getMaxHealth();
        healthBar = new ProgressBar(0, maxHP, 1, false, baseStyle);

        healthBar.setValue(gameWorld.getPlayer().getStats().getHealth());

        healthBar.setAnimateDuration(0.1f);

        // 8) Left column for the bar
        Table leftTable = new Table();
        leftTable.padLeft(20);
        leftTable.add(healthBar)
            .width(180)
            .left();
        leftTable.row().padTop(8);
        leftTable.add(staminaBar)
            .width(180)
            .left();

        // 9) Right column for skull/world
        Table rightTable = new Table();
        rightTable.padRight(20);
        rightTable.add(skullLabel)
            .left()
            .padBottom(20)
            .row();
        rightTable.add(worldLabel)
            .left();

        // 10) Place them side-by-side in the HUD
        hud.add(leftTable).expandX().left();
        hud.add(rightTable).expandX().right();
    }

    private void createPauseOverlay() {
        // This Table covers the entire screen and darkens the background
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);

        // A semi-transparent background (dark overlay)
        pauseOverlay.setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));

        // Add table to the uiStage
        uiStage.addActor(pauseOverlay);

        // create an inner table to hold the actual menu buttons
        Table menuTable = new Table();
        //center it
        menuTable.center();
        pauseOverlay.add(menuTable);


        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton optionsButton = new TextButton("Options", skin);
        TextButton exitButton = new TextButton("Exit to Main Menu", skin);

        // Add all to menuTable
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


                pauseOverlay.setVisible(false);

                OptionsOverlay myOverlay = new OptionsOverlay(skin);
                myOverlay.setFillParent(true);
                myOverlay.setModal(true);
                myOverlay.setBackground(skin.newDrawable("white", 0, 0, 0, 0.8f));


                Table content = new Table();
                content.center();
                myOverlay.add(content).expand().fill();



                myOverlay.setOptionsListener(new OptionsOverlay.OptionsListener() {
                    @Override
                    public void onShowControls() {

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

                        myOverlay.remove();
                        pauseOverlay.setVisible(true);
                    }
                });
                uiStage.addActor(myOverlay);

                // 4) stage
                float stageWidth = uiStage.getViewport().getWorldWidth();
                float stageHeight = uiStage.getViewport().getWorldHeight();
                myOverlay.setPosition((stageWidth - myOverlay.getWidth()) / 2f,
                    (stageHeight - myOverlay.getHeight()) / 2f);
            }
        });



        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentMusic != null && currentMusic.isPlaying()) {
                    currentMusic.stop();
                }
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

        staminaBar.setValue(gameWorld.getPlayer().getStats().getStamina());
        healthBar.setValue(gameWorld.getPlayer().getStats().getHealth());
        checkPlayerDeath();
        // 1) Update logic & stages
        if (!paused) {
            gameWorld.update(delta);
            stage.act(delta);
            overlayStage.act(delta);
        }
        gameWorld.getInventory().draw(uiStage);

        //1.5) handle doors

        Door door = gameWorld.getOverlappingDoor();
        if (door != null && Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            handleDoor(door);
        }


        // 1.7) if the player isn’t spawned yet, skip camera + rest
        if (gameWorld.getPlayer() == null) {
            // still loading/spawning
            // draw UI and bail out early:
            uiStage.act(delta);
            uiStage.draw();
            return;
        }
        // 2) Center camera on player horizontally, but clamp to world borders:
        float playerCenterX = gameWorld.getPlayer().getX()
            + gameWorld.getPlayer().getWidth() / 2f;

        // clamp so camera never goes beyond the left/right border
        float camX = MathUtils.clamp(playerCenterX, minCameraX, maxCameraX);

        // compute vertical
        float playerCenterY = gameWorld.getPlayer().getY()
            + gameWorld.getPlayer().getHeight() / 2f;
        float desiredYOffset = MathUtils.clamp(
            (playerCenterY + cameraOffsetY) - initialCamY,
            0f,
            maxCameraYOffset
        );
        float targetCamY = initialCamY + desiredYOffset;
        float lerpFactor = MathUtils.clamp(delta * cameraSmoothFactor, 0f, 1f);
        float smoothCamY = MathUtils.lerp(camera.position.y, targetCamY, lerpFactor);

        // set and update camera
        camera.position.set(camX, smoothCamY, 0f);
        camera.update();


        stage.getViewport().apply();

        // 3) Clear screen
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 4) Parallax pass
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float camLeft   = camera.position.x - viewportWidth  / 2f;
        float camBottom = camera.position.y - viewportHeight / 2f;

        float cloudVFactor  = 0.1f;
        float trunkVFactor  = 0.2f;
        float maxVOffset = 50f;

        // how far we’ve moved from rest
        float deltaY     = camera.position.y - initialCamY;

        // 4.1) Sky
        batch.draw(bgSky, camLeft, camBottom, viewportWidth, viewportHeight);

        // 4.2) Sun
        float sunScale = 0.5f; // adjust size
        float sunW = bgSun.getWidth() * sunScale;
        float sunH = bgSun.getHeight() * sunScale;


        // raw parallax offsets
        float rawCloudOffset = deltaY * cloudVFactor;
        float rawTrunkOffset = deltaY * trunkVFactor;

        // clamp so offset ≤ maxVOffset
        float cloudYOffset = MathUtils.clamp(rawCloudOffset, -maxVOffset, maxVOffset);
        float trunkYOffset = MathUtils.clamp(rawTrunkOffset, -maxVOffset, maxVOffset);

        batch.draw(
            bgSun,
            camLeft + viewportWidth * 0.2f - sunW/2,
            camBottom + viewportHeight * 0.7f - sunH/2,
            sunW,
            sunH
        );

        // 4.3) Side‐clouds
        batch.setColor(1f, 1f, 1f, 0.75f);
        drawTiledLayer(
            batch,
            bgSideClouds,
            camLeft,
            camBottom + viewportHeight * 0f,
            -0.005f,
            0.4f
        );
        batch.setColor(1f, 1f, 1f, 1f);

        // 4.4) Mountains
        drawTiledLayer(batch,
            bgMountains,
            camLeft,
            camBottom + 40f,
            -0.025f,
            0.5f);

        // 4.5) Top‐clouds
        drawTiledLayer(
            batch,
            bgTopClouds,
            camLeft,
            camBottom + viewportHeight * 0.73f,
            -0.13f,
            0.6f
        );

        // 4.6) Small‐clouds
        drawTiledLayer(
            batch,
            bgSmallClouds,
            camLeft,
            camBottom + viewportHeight * 0.4f + trunkYOffset ,
            -0.12f,
            0f
        );

        // 4.7) Big‐tree trunk

        drawTiledLayer(
            batch,
            bgBigTree,
            camLeft,
            camBottom + viewportHeight * 0.1f,
            -0.15f,
            0.7f
        );
        // 4.8) Meadow
        drawTiledLayer(
            batch,
            bgMeadow,
            camLeft,
            camBottom + viewportHeight * -0.05f,
            -0.2f,
            0.225f
        );

        batch.end();
        // 5)draw the world
        stage.draw();

        // 6) Any overlay Stage
        overlayStage.setViewport(stage.getViewport());
        overlayStage.draw();

        // 7) Your debug‐shape passes
        shapeRenderer.setProjectionMatrix(camera.combined);

        // filled‐shape pass
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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


        int rad = CurrentDificulty.getRadiation();
        Color tintColor;
        switch (CurrentDificulty.getRadiation()) {
            case 2:
                tintColor = new Color(1f, 0.5f, 0f, 0.12f);  // orange
                break;
            case 3:
                tintColor = new Color(0f, 0f, 0f, 0.30f);    // black
                break;
            default:
                tintColor = new Color(0.4f, 0f, 0.2f, 0.12f); // pink
                break;
        }

        // 8) Simple dark-pink full-screen tint
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(tintColor);
        // compute the bottom-left corner of the camera’s view in world coords
        float tintX = camera.position.x - viewportWidth  / 2f;
        float tintY = camera.position.y - viewportHeight / 2f;
        // draw a rectangle that exactly covers the screen
        shapeRenderer.rect(tintX, tintY, viewportWidth, viewportHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (rad >= 2) {
            batch.begin();

            // Pick a grain‐opacity per radiation level
            float grainAlpha;
            if (rad == 2) {
                grainAlpha = 0.25f;
            } else { // rad == 3
                grainAlpha = 0.4f;
            }
            batch.setColor(1f, 1f, 1f, grainAlpha);

            // Tile your noise texture over the screen
            float tileW = grainTex.getWidth();
            float tileH = grainTex.getHeight();
            for (float x = camLeft; x < camLeft + viewportWidth; x += tileW) {
                for (float y = camBottom; y < camBottom + viewportHeight; y += tileH) {
                    batch.draw(grainTex, x, y, tileW, tileH);
                }
            }

            batch.setColor(Color.WHITE);
            batch.end();
        }

        float currentStam = gameWorld.getPlayer().getStats().getStamina();
        staminaBar.setValue(currentStam);

        // 9) Finally the UI
        uiStage.act(delta);
        uiStage.draw();

    }

    private void handleDoor(Door door) {
        if (door.getType() == Door.Type.EXTRACT) {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }
            gameWorld.extractItems();
            inventory = new Inventory(skin);

            game.setScreen(new MainScreen(game));
        } else {
            // CONTINUE door --> first record the player's choice
            CurrentDificulty.setRadiation(door.getRadiationLevel());
            // then advance or continue the run
            if (runMgr.isFinalWorld()) {
                runMgr.continueRun();

            } else {
                runMgr.advanceWorld();
                gameWorld.giveMoney();
            }

            //Updates global difficulty
            int newDiff = runMgr.getSkullLevel() * 5 + runMgr.getWorldLevel();
            CurrentDificulty.setDifficulty(newDiff);

            updateHud();

            // reload all themed assets & rebuild the world
            int newRad = CurrentDificulty.getRadiation();
            String newFolder = (newRad == 2 ? "med" : newRad == 3 ? "high" : "low");
            loadBackgrounds(newFolder);
            TreeTile.loadForRadiation(newFolder);
            GrassOverlayTile.loadForRadiation(newFolder);
            DecorTile.loadForRadiation(newFolder);
            PlatformGrassOverlayTile.loadForRadiation(newFolder);
            loadCurrentWorld();
            switchMusic(newRad);
        }
    }

    private void updateHud() {
        skullLabel.setText("Skull: " + runMgr.getSkullLevel());
        worldLabel.setText("World: " + runMgr.getWorldLevel());
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

    private void checkPlayerDeath() {
        PlayerActor p = gameWorld.getPlayer();
        if (p != null && p.getStats().getHealth() <= 0) {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }
            game.setScreen(new DeathScreen(game));
        }
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
        bgMeadow.dispose();
        if (grainTex != null) grainTex.dispose();
        if (musicRad2   != null) musicRad2.dispose();
        if (musicRad3   != null) musicRad3.dispose();
        if (bossMusic != null)  bossMusic.dispose();
    }
}
