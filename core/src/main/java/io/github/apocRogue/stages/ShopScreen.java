package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.shop.ShopUI;
import io.github.apocRogue.stages.stageBuilder;

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
        // Set up the stage & skin
        stage = new Stage(new FitViewport(1080, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));


        // Initialise & draw the player inventory UI
        Inventory inv = new Inventory(skin);
        inv.draw(stage);

        // Build the ShopUI
        shopUI = new ShopUI(
            stage,
            skin,
            game
        );

        // Set input to this stage
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
