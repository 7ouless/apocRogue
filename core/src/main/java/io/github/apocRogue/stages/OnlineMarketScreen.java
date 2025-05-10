package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.JsonValue;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnlineMarketScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;

    public OnlineMarketScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 600));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table(skin);
        root.setFillParent(true);
        root.pad(20);

        Label title = new Label("Online Market", skin);
        title.setFontScale(1.5f);
        root.add(title).colspan(3).padBottom(20f).row();

        // Header row
        root.add(new Label("Item", skin)).width(200);
        root.add(new Label("Price", skin)).width(100);
        root.add(new Label("Action", skin)).width(100).row();


        // Fetch market data
        DBManager.get().fetchMarket(new JsonCallback() {
            @Override
            public void onSuccess(String json) {
                // Not used
            }

            @Override
            public void onSuccess(JsonValue data) {
                Gdx.app.postRunnable(() -> {
                    // Clear existing rows
                    root.clearChildren();
                    root.add(title).colspan(3).padBottom(20f).row();
                    root.add(new Label("Item", skin)).width(200);
                    root.add(new Label("Price", skin)).width(100);
                    root.add(new Label("Action", skin)).width(100).row();

                    // Populate new rows
                    for (JsonValue item : data.get("listings")) {
                        String code = item.getString("itemCode");
                        long price = item.getLong("price");

                        root.add(new Label(code, skin)).width(200);
                        root.add(new Label(String.valueOf(price), skin)).width(100);

                        TextButton buyBtn = new TextButton("Buy", skin);
                        buyBtn.addListener(new ChangeListener() {
                            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                                // TODO: call DBManager.get().buy(listingId,...)
                            }
                        });
                        // inside your existing loop over data.get("listings"):

                        long listingId = item.getLong("listingID");

                        root.add(new Label(code, skin)).width(200);
                        root.add(new Label(String.valueOf(price), skin)).width(100);

                        buyBtn.addListener(new ChangeListener() {
                            @Override public void changed(ChangeEvent event, Actor actor) {
                                // disable the button until done
                                buyBtn.setDisabled(true);

                                // call your new buy(...)
                                DBManager.get().buy(listingId, new JsonCallback() {
                                    @Override public void onSuccess(String json) {}
                                    @Override public void onSuccess(JsonValue data) {
                                        Gdx.app.postRunnable(() -> {
                                            Dialog d = new Dialog("Success", skin);
                                            d.text("Purchased!");
                                            d.button("OK");
                                            d.show(stage);
                                            // refresh the market list
                                            show();  // re-run show() to re-fetch & redraw
                                        });
                                    }
                                    @Override public void onError(Throwable t) {
                                        Gdx.app.postRunnable(() -> {
                                            Dialog d = new Dialog("Error", skin);
                                            d.text("Purchase failed:\n" + t.getMessage());
                                            d.button("OK");
                                            d.show(stage);
                                            buyBtn.setDisabled(false);
                                        });
                                    }
                                });
                            }
                        });
                        root.add(buyBtn).width(100).row();
                    }

                    stage.addActor(root);
                });
            }

            @Override
            public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    Dialog d = new Dialog("Error", skin);
                    d.text("Failed to load market.");
                    d.button("OK");
                    d.show(stage);
                });
            }
        });


        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);

    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
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
