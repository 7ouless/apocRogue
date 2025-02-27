package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {
    private stageBuilder game;

    // One stage for both game world + UI
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Stage stage;

    // Player
    private PlayerActor player;
    private Texture playerTexture;

    // UI elements
    private InventoryUI inventoryUI;  // We'll let it create a hotbar and inventory
    private boolean inventoryOpen = false;

    // Misc
    private SpriteBatch batch;
    private boolean useProcedural = true;

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 1) Create camera & FitViewport
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        viewport = new FitViewport(1080, 720, camera);

        // 2) Create one Stage for everything
        stage = new Stage(viewport);

        stage.addListener(new InputListener() {
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                // 'amount' > 0 means scrolled down on some platforms,
                // might be reversed on others.
                System.out.println("Mouse wheel scrolled: amount=" + amount);

                // Test to confirm the direction you want.
                if (amount > 0) {
                    // e.g. scroll forward
                    inventoryUI.updateSlotSelection(+1);
                } else {
                    // scroll backward
                    inventoryUI.updateSlotSelection(-1);
                }
                return true;
            }
        });
        // 3) Create a batch
        batch = new SpriteBatch();

        // 4) Load player & add to stage
        playerTexture = new Texture("ui/sprite.png");
        player = new PlayerActor(playerTexture);
        player.setPosition(50, 100);
        stage.addActor(player);

        // 5) Build the map (using a single stage)
        GenerationSettings settings = new GenerationSettings();
        settings.levelWidth = 3000;
        settings.platformDensity = 5;
        MapManager mapManager = new MapManager(settings);
        mapManager.setUseProcedural(useProcedural);
        mapManager.generateMap(stage);

        // 6) Create the InventoryUI inside the same Stage
        // We'll let InventoryUI build its hotbar & inventoryWindow, but also
        // add them to the *same* stage. So we pass the same Stage, not a separate one.
        inventoryUI = new InventoryUI(stage);

        // Default input to this stage
        Gdx.input.setInputProcessor(stage);
        stage.draw();
    }

    @Override
    public void render(float delta) {
        // 1) Update logic
        stage.act(delta);

        // 2) Clear screen
        ScreenUtils.clear(0, 0, 0, 1);

        // 3) Camera follows the player
        camera.position.set(
            player.getX() + player.getWidth() / 2f,
            player.getY() + player.getHeight() / 2f,
            0
        );
        camera.update();

        // 4) Position the UI so it appears "locked" to the screen
        //    We do manual layout each frame in camera coords.

        float stageW = viewport.getWorldWidth();
        float stageH = viewport.getWorldHeight();

        // The camera center is (camera.position.x, camera.position.y)
        // The top-left corner of the screen is:
        float leftX = camera.position.x - stageW / 2f;
        float topY  = camera.position.y + stageH / 2f;

        // If the user wants the hotbar at the top-left corner of the screen,
        // we place it relative to (leftX, topY).
        inventoryUI.positionHotbar(leftX, topY);

        // If the inventory is open, we place it in the middle of the screen

        // 5) Check for TAB to toggle inventory
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            inventoryOpen = !inventoryOpen;
            inventoryUI.setInventoryOpen(inventoryOpen);
        }
        if (inventoryOpen) {
            inventoryUI.positionInventory(camera.position.x, camera.position.y);
        }

// Toggle inventory with TAB
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            inventoryOpen = !inventoryOpen;
            inventoryUI.setInventoryOpen(inventoryOpen);
        }

// 1) Let the user select a slot (left/right or number keys)
        // 6) Switch input if inventory is open
        //    (If you want to let the user click on the inventory, you might skip this
        //     or handle it differently. Right now, we keep it all in one stage, so
        //     we don't need to swap input processors.)
        // if (inventoryOpen) { ... } else { ... }
        stage.draw();


    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        inventoryUI.dispose();
        batch.dispose();
        if (playerTexture != null) playerTexture.dispose();
    }
}
