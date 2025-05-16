package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.apocRogue.OMarket.MarketListing;
import io.github.apocRogue.OMarket.MarketService;
import io.github.apocRogue.OMarket.MarketUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class OnlineMarketScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;
    private MarketUI marketUI;

    public OnlineMarketScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Initialise Stage & Skin
        stage = new Stage(new FitViewport(1080, 720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Build the Market UI
        marketUI = new MarketUI(stage, skin, game);
        marketUI.build();


        TextButton backButton = new TextButton("Back", skin);
        backButton.pad(10f);
        // place 20 px from the top‑left corner of the virtual viewport
        backButton.setPosition(
            stage.getViewport().getWorldWidth() - backButton.getWidth() - 20,
            stage.getViewport().getWorldHeight() - backButton.getHeight() - 20);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainScreen(game));
            }
        });
        stage.addActor(backButton);

        // Route input to our UI
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update & draw stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        // Clean up resources
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }
}
