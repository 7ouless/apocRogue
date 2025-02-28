package io.github.apocRogue;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private Stage uiStage;
    private Skin skin;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerTexture;
    private PlayerActor player;
    private Inventory inventory;
    private stageBuilder game;

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Set up game camera and stage.
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        stage = new Stage(new FitViewport(1080, 720, camera));

        // Set up UI stage with a ScreenViewport so it stays fixed on the screen.
        uiStage = new Stage(new ScreenViewport());

        // Load skin and create batch.
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        batch = new SpriteBatch();

        // Load player texture and create the player actor.
        playerTexture = new Texture(Gdx.files.internal("ui/sprite.png"));
        player = new PlayerActor(playerTexture);
        player.setPosition(50, 100); // position near the bottom
        stage.addActor(player);

        // Create generation settings and generate the map.
        GenerationSettings settings = new GenerationSettings();
        settings.levelWidth = 3000;
        settings.platformDensity = 5;
        MapManager mapManager = new MapManager();
        mapManager.generateMap(stage);

        // Create inventory UI and add it to the UI stage.
        inventory = new Inventory(skin);
        inventory.draw(uiStage);

        // Use an InputMultiplexer so both game stage and UI stage get input.
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                inventory.scrollHotbar((int) amountY);
                return true;
            }
        });
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // Listen for key events on the UI stage.
        uiStage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // Toggle inventory panel with Tab.
                if (keycode == Input.Keys.TAB) {
                    inventory.toggleInventory();
                    return true;
                }
                // Number keys 1-5 select corresponding hotbar slots.
                if (keycode == Input.Keys.NUM_1) {
                    inventory.setSelectedHotbarIndex(0);
                    return true;
                }
                if (keycode == Input.Keys.NUM_2) {
                    inventory.setSelectedHotbarIndex(1);
                    return true;
                }
                if (keycode == Input.Keys.NUM_3) {
                    inventory.setSelectedHotbarIndex(2);
                    return true;
                }
                if (keycode == Input.Keys.NUM_4) {
                    inventory.setSelectedHotbarIndex(3);
                    return true;
                }
                if (keycode == Input.Keys.NUM_5) {
                    inventory.setSelectedHotbarIndex(4);
                    return true;
                }
                return false;
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                // Scroll to change the selected hotbar slot.
                inventory.scrollHotbar(amount);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Update both stages.
        stage.act(delta);
        uiStage.act(delta);

        // Follow the player.
        camera.position.set(player.getX() + player.getWidth() / 2f,
            player.getY() + player.getHeight() / 2f, 0);
        camera.update();
        stage.getViewport().apply();
        batch.setProjectionMatrix(camera.combined);

        // Draw the game and UI.
        stage.draw();
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        uiStage.getViewport().update(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        uiStage.dispose();
        skin.dispose();
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        batch.dispose();
    }
}
