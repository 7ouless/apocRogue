package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;

import java.util.Map;

public class MainScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;

    public MainScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1080, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Window window = new Window("APOC ROGUE", skin, "border");
        window.defaults().pad(4f);
        window.add("MAIN MENU").row();

        TextButton buttonOpen = new TextButton("DEPLOY!", skin);
        buttonOpen.pad(8f);
        buttonOpen.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
            }
        });
        window.add(buttonOpen).row();

        TextButton invBtn = new TextButton("Inventory", skin);
        invBtn.pad(8f);
        invBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new InventoryScreen(game));
            }
        });
        window.add(invBtn).row();

        TextButton shopBtn = new TextButton("Shop", skin);
        shopBtn.pad(8f);
        shopBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new ShopScreen(game));
            }
        });
        window.add(shopBtn).row();

        TextButton marketBtn = new TextButton("Market", skin);
        marketBtn.pad(8f);
        marketBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new OnlineMarketScreen(game));
            }
        });
        window.add(marketBtn).row();

        TextButton logoutBtn = new TextButton("Logout", skin);
        logoutBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LoginScreen(game));
            }
        });
        window.add(logoutBtn).row();

        window.pack();
        window.setPosition(
            MathUtils.roundPositive(stage.getWidth() / 2f - window.getWidth() / 2f),
            MathUtils.roundPositive(stage.getHeight() / 2f - window.getHeight() / 2f)
        );
        stage.addActor(window);

        // Set input processor
        Gdx.input.setInputProcessor(stage);

        // -- TEST: push a dummy inventory item via DBManager --
        Map<String, Integer> testItems = Map.of(
            "ID04A1B2C3D4E5F6A7B8C9", 1
        );
        DBManager.get().pushInventory(testItems, new JsonCallback() {
            @Override
            public void onSuccess(String json) {

            }

            @Override
            public void onSuccess(JsonValue result) {
                Gdx.app.log("MainScreen", "Inventory push success: " + result.toString());
            }

            @Override
            public void onError(Throwable t) {
                Gdx.app.error("MainScreen", "Inventory push error", t);
            }

            @Override
            public void onFailure(String error) {
                Gdx.app.error("MainScreen", "Inventory push failed: " + error);
            }
        });
    }


    @Override
    public void render(float delta) {
        // Clears background so you only see this screen’s UI
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        stage.act(delta);
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
    }
}
