package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class MainScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;
    private SpriteBatch batch;
    private Texture bgTex;
    private float bgScale = 1.2f;

    public MainScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        bgTex = new Texture(Gdx.files.internal("ui/main-menu-bg.png"));

        stage = new Stage(new FillViewport(1080, 720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // 1) Add a big centered title at the top
        float vw = stage.getViewport().getWorldWidth();
        Label title = new Label("KIGEN", skin);
        title.setFontScale(3f);
        title.setAlignment(Align.center);
        float topMargin = 320f;
        float rightShift = 35f;  // tweak this until it’s where you like
        float x = (vw - title.getWidth()*title.getFontScaleX())/2f + rightShift;
        float y = stage.getViewport().getWorldHeight() - topMargin;
        title.setPosition(x, y);
        stage.addActor(title);

        // 2) Build your vertical button container (no grey background or border)
        Window window = new Window("", skin);
        window.setBackground((Drawable)null);
        final float BUTTON_WIDTH = 200f;

        window.columnDefaults(0).width(BUTTON_WIDTH);


        // bump up the default padding so buttons are more spread out:
        window.defaults().pad(12f);
        TextButton deploy = new TextButton("DEPLOY!", skin);
        deploy.pad(8f);
        deploy.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new GameScreen(game));
            }
        });
        window.add(deploy).row();

        TextButton inv = new TextButton("Inventory", skin);
        inv.pad(8f);
        inv.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new InventoryScreen(game));
            }
        });
        window.add(inv).row();

        TextButton shop = new TextButton("Shop", skin);
        shop.pad(8f);
        shop.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new ShopScreen(game));
            }
        });
        window.add(shop).row();

        TextButton market = new TextButton("Market", skin);
        market.pad(8f);
        market.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new OnlineMarketScreen(game));
            }
        });
        window.add(market).row();

        TextButton logout = new TextButton("Logout", skin);
        logout.pad(8f);
        logout.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new LoginScreen(game));
            }
        });
        window.add(logout).row();

        // 3) Position the window at the bottom‐center
        window.pack();
        float wx = (vw - window.getWidth()) / 2f;
        float wy = 120f;               // 20px above the bottom edge
        window.setPosition(wx, wy);

        stage.addActor(window);
        Gdx.input.setInputProcessor(stage);
    }


    @Override
    public void render(float delta) {
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();

        // 1) Original pixel size of your texture
        float texW = bgTex.getWidth();
        float texH = bgTex.getHeight();

        // 2) Figure out the scale so that BOTH width and height are >= viewport
        float scaleX = vw / texW;
        float scaleY = vh / texH;
        float coverScale = Math.max(scaleX, scaleY);

        // 3) Optional extra zoom factor (1.0 = exact cover, >1 = zoom in more)
        float zoom = bgScale;  // e.g. 1.0f or 1.2f
        float finalScale = coverScale * zoom;

        // 4) Compute drawn size
        float w = texW * finalScale;
        float h = texH * finalScale;

        // 5) Center the image so overflow is equal on both sides
        float x = (vw - w) / 2f;
        float y = (vh - h) / 2f + 50f;

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(bgTex, x, y, w, h);
        batch.end();

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
        batch.dispose();
        bgTex.dispose();
    }
}
