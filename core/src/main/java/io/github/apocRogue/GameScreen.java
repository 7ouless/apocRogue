package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    // Textures
    private Texture playerTexture;
    // Actors
    private PlayerActor player;

    public GameScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        //create the camera and set the viewport size
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        stage = new Stage(new FitViewport(1080, 720, camera));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load the texture for the player
        playerTexture = new Texture(Gdx.files.internal("ui/sprite.png"));

        batch = new SpriteBatch();

        // Create the player actor
        player = new PlayerActor(playerTexture);
        // Start it somewhere above the ground
        player.setPosition(50, 300);
        stage.addActor(player);

        // Create a ground actor along the bottom: (x=0, y=0, width=1080, height=50)
        FloorActor ground = new FloorActor(0, 0, 1080, 50);
        stage.addActor(ground);

        // Create the "Go Back" button in a window at bottom-right
        Window window = new Window("Game Screen", skin, "border");
        window.defaults().pad(4f);
        window.add("MainGame").row();

        TextButton goBack = new TextButton("Go Back!", skin);
        goBack.pad(8f);
        goBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to the main menu screen
                game.setScreen(new MainScreen(game));
            }
        });
        window.add(goBack);
        window.pack();
        // Position the window in the bottom-right corner
        window.setPosition(stage.getWidth() - window.getWidth() - 10, 10);
        stage.addActor(window);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        stage.act(delta);

        // Move the camera BEFORE drawing
        camera.position.set(player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f, 0);
        camera.update();

        // Ensure the stage uses the updated camera
        stage.getViewport().apply();

        batch.setProjectionMatrix(camera.combined);

        // Only call stage.draw() ONCE
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
        playerTexture.dispose();
    }
}
