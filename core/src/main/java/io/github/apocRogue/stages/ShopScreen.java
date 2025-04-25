package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.inventory.general.ItemManager;
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
        List<ShopKeeper> shopkeepers = ShopInventory.loadShopkeepers();

        // Initialise our ShopUI, passing in game
        ItemManager mgr = new ItemManager();
        mgr.loadFromFile("ui/items.json");    // now mgr.loadedWeapons contains Sword,Bow,HandGun

        Inventory inv = new Inventory(skin);   // your game‐inventory UI
        inv.draw(stage);                       // or however you show it

        shopUI = new ShopUI(stage,
            skin,
            shopkeepers,
            game,
            mgr,
            inv);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Update the shop UI logic (including typing effect)
        shopUI.update(delta);

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
