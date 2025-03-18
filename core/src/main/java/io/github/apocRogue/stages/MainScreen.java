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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

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
                // Goes to actual game screen
                game.setScreen(new GameScreen(game));
            }
        });
        window.add(buttonOpen).row();

        TextButton shopBtn = new TextButton("Shop", skin);
        shopBtn.pad(8f);
        shopBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                // Switch to shop screen
                game.setScreen(new ShopScreen(game));
            }
        });
        window.add(shopBtn).row();

        window.pack();
        window.setPosition(
            MathUtils.roundPositive(stage.getWidth() / 2f - window.getWidth() / 2f),
            MathUtils.roundPositive(stage.getHeight() / 2f - window.getHeight() / 2f)
        );
        stage.addActor(window);
        Gdx.input.setInputProcessor(stage);
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
