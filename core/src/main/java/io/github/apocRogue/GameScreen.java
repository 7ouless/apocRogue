package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Main game screen that sets up the stage, camera, and calls MapManager to build the map.
 */
public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;      // your main Game class
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Texture playerTexture;
    private PlayerActor player;
    private boolean useProcedural = true;

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Camera & viewport
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Setup stage & UI
        stage = new Stage(new FitViewport(1080, 720, camera));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        batch = new SpriteBatch();

        // Load player texture (IMPORTANT: do this before creating PlayerActor)
        playerTexture = new Texture(Gdx.files.internal("ui/sprite.png"));
        player = new PlayerActor(playerTexture);
        player.setPosition(50, 100); // place the player somewhere near the bottom
        stage.addActor(player);

        // Create generation settings
        GenerationSettings settings = GenerationType.PLAINS.settings;
        // tweak minGap, maxGap, etc. if you like

        // Create a MapManager and build the map
        MapManager mapManager = new MapManager();
        mapManager.generateMap(stage);

        // Set stage as input processor
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        stage.act(delta);

        // Follow the player
        camera.position.set(
            player.getX() + player.getWidth() / 2f,
            player.getY() + player.getHeight() / 2f,
            0
        );
        camera.update();
        stage.getViewport().apply();
        batch.setProjectionMatrix(camera.combined);

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        batch.dispose();
    }
}
