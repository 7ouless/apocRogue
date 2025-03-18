package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.shop.ShopKeeper;
import io.github.apocRogue.shop.ShopUI;
import io.github.apocRogue.shop.ShopInventory;

import java.util.List;

public class ShopScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;
    private ShopUI shopUI;

    public ShopScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Separate stage just for the shop
        stage = new Stage(new FitViewport(1080, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load the shopkeepers
        List<ShopKeeper> shopkeepers = ShopInventory.loadShopkeepers(skin);

        // Initialize our ShopUI, passing in game
        shopUI = new ShopUI(stage, skin, shopkeepers, game);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear the background to black so the old menu isn’t visible
        ScreenUtils.clear(0, 0, 0, 1);

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
