package io.github.apocRogue.stages;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.apocRogue.actors.ChestActor;
import io.github.apocRogue.actors.DummyActor;
import io.github.apocRogue.actors.PlayerActor;
import io.github.apocRogue.inventory.Inventory;
import io.github.apocRogue.inventory.ItemManager;
import io.github.apocRogue.map.GenerationSettings;
import io.github.apocRogue.map.RoomCreator;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.map.MapCreator;

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
        camera.setToOrtho(false, 1920, 1080);
        stage = new Stage(new FitViewport(1920, 1080, camera));

        // Set up UI stage with a ScreenViewport so it stays fixed on the screen.
        uiStage = new Stage(new FitViewport(1920, 1080));
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
        settings.roomWidth = 3000;
        settings.platformDensity = 5;
        MapCreator mc = new MapCreator();
        RoomCreator roomCreator = new RoomCreator();
        roomCreator.generateMap(stage);

// Update player's starting position based on generated ground.
        player.setPosition(50, roomCreator.settings.groundMax);
        float spawnYOffset = 10; // Adjust this offset as needed
        float spawnY = roomCreator.settings.groundMax + spawnYOffset;

        // Create inventory UI and add it to the UI stage.
        inventory = new Inventory(skin);
        player.setInventory(inventory);

        inventory.draw(uiStage);
        Gdx.app.log("StageSize", "UI stage world width="
            + uiStage.getViewport().getWorldWidth()
            + ", height=" + uiStage.getViewport().getWorldHeight());
        // Use an InputMultiplexer so both game stage and UI stage get input.
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                inventory.scrollHotbar((int) amountY);
                return true;
            }
        });
        uiStage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Weapon w = inventory.getSelectedWeapon();
                    if (w != null) {
                        w.use(player, stage);
                    }
                    // Return true if you want to consume the event
                    return true;
                }
                return false;
            }
        });
        Texture dummyTexture = new Texture("ui/dummy.png");
        DummyActor dummy = new DummyActor(dummyTexture, 400, spawnY);
        Texture chestTexture = new Texture("ui/chest.png");

        // Create a list of possible items for the chest to drop
        ItemManager itemManager = new ItemManager();
        itemManager.loadFromFile("items.json"); // your JSON file

        // Now you have an Array<Weapon> with all items
        Array<Weapon> allWeapons = itemManager.getLoadedWeapons();

        // If you want to create a chest with random items from that list:
        ChestActor chest = new ChestActor(chestTexture, 500, spawnY, allWeapons, skin);
        stage.addActor(chest);
        stage.addActor(chest);
        stage.addActor(dummy);
        multiplexer.addProcessor(uiStage);   // UI first
        multiplexer.addProcessor(stage);     // Game second
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
        uiStage.setDebugAll(true);
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
        if(Gdx.input.isKeyJustPressed(Input.Buttons.LEFT)) {
            Weapon w = inventory.getSelectedWeapon();
            if (w != null) {
                w.use(player, stage);

            }
        }
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
