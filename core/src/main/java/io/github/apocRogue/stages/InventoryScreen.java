package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;               // ← new
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;
import io.github.apocRogue.inventory.general.InventoryItem;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.inventory.menuinventory.InventoryUI;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class InventoryScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;
    private InventoryUI inventoryUI;
    private WeaponTypeRegistry typeRegistry;
    public InventoryScreen(stageBuilder game) {
        this.game = game;
    }
    public void show() {
        stage = new Stage(new FitViewport(1080, 720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // load your weapon types
        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");

        inventoryUI = new InventoryUI(stage, skin, game);
        Gdx.input.setInputProcessor(stage);

        DBManager.get().fetchInventory(new JsonCallback() {
            @Override public void onSuccess(String json) { /* unused */ }

            @Override
            public void onSuccess(JsonValue result) {
                Gdx.app.postRunnable(() -> {
                    List<InventoryItem> stash = new ArrayList<>();
                    for (JsonValue it : result) {
                        stash.add(new InventoryItem(
                            it.getString("itemCode"),
                            it.getString("typeID"),
                            typeRegistry.get(it.getString("typeID")).getName(),
                            typeRegistry.get(it.getString("typeID")).getTexturePath()
                        ));
                    }
                    inventoryUI.setStashItems(stash);         // now resolved
                    inventoryUI.refreshStashGrid();
                });
            }
            @Override public void onError(Throwable t) { /* … */ }
            @Override public void onFailure(String error) { /* … */ }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
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
