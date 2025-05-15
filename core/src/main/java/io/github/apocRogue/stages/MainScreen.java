package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.Color;

public class MainScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;
    private SpriteBatch batch;
    private Texture normalBg, deadBg;
    private Texture grainTex;
    private Texture bgTex;
    private enum State { WAITING, FLASHING, HOLD, SCARED }
    private State state = State.WAITING;
    private float nextTrigger, timer,  holdTimer, scareTimer, grainAlpha;
    private float bgScale = 1.2f;

    public MainScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        normalBg = new Texture(Gdx.files.internal("ui/main-menu-bg.png"));
        deadBg   = new Texture(Gdx.files.internal("ui/main-menu-bg-dead.png"));
        bgTex    = normalBg;

        stage = new Stage(new FillViewport(1080, 720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        //Age Recommendation
        Dialog ageDialog = new Dialog("", skin);
        ageDialog.getContentTable().pad(15);
        ageDialog.getButtonTable().padTop(10).padBottom(10);
        ageDialog.text(
            "Contains themes of nuclear disaster and stylised combat. Recommended 12+."
        );
        ageDialog.button("Acknowledge");
        ageDialog.show(stage);

        //Tutorial Button
        Texture tutTex = new Texture(Gdx.files.internal("ui/tutorial.png"));
        ImageButton tutorialButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(tutTex))
        );

        tutorialButton.setTransform(true);
        tutorialButton.setScale(0.07f);

        tutorialButton.setPosition(10, 60);

        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new Tutorial(skin).show(stage);
            }
        });

        stage.addActor(tutorialButton);

        // 1) Add a big centered title at the top
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();
        float dw = ageDialog.getWidth();
        float dh = ageDialog.getHeight();

        ageDialog.setPosition(
            (vw - dw) / 2,
            vh * 0.75f - dh / 2
        );
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

        // prepare a simple repeating noise texture
        Pixmap pix = new Pixmap(64,64,Pixmap.Format.RGBA8888);
        for(int ix = 0; ix < 64; ix++){
            for(int iy = 0; iy < 64; iy++){
                float a = MathUtils.random() * 0.3f;
                pix.setColor(1,1,1,a);
                pix.drawPixel(ix, iy);
                }
            }
        grainTex = new Texture(pix);
        grainTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pix.dispose();

        // schedule first scare in 10–20s
        scheduleNext();
    }



    private void scheduleNext() {
        nextTrigger = MathUtils.random(5f,12f);
        timer = 0;
        state = State.WAITING;
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
        timer += delta;

        switch(state) {
            case WAITING:
                if (timer >= nextTrigger) {
                    timer      = 0f;
                    grainAlpha = 0f;
                    state      = State.FLASHING;
                }
                break;

            case FLASHING:
                // ramp grain up more slowly:
                grainAlpha = Math.min(1f, grainAlpha + delta * 1f);  // try 1f instead of 5f
                if (grainAlpha >= 1f) {
                    holdTimer = 0f;
                    state     = State.HOLD;
                }
                break;

            case HOLD:
                holdTimer += delta;
                if (holdTimer >= 0.5f) {
                    // after 1 second at full grain, switch to dead BG and clear grain
                    bgTex      = deadBg;
                    grainAlpha = 0f;
                    scareTimer = 0f;
                    state      = State.SCARED;
                }
                break;

            case SCARED:
                scareTimer += delta;
                if (scareTimer >= 0.5f) {
                    bgTex      = normalBg;
                    grainAlpha = 0f;
                    scheduleNext();
                }
                break;
        }

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(bgTex, x, y, w, h);

        if ((state == State.FLASHING || state == State.HOLD) && grainAlpha > 0f) {
            batch.setColor(1f,1f,1f, grainAlpha);
            float tileW = grainTex.getWidth(), tileH = grainTex.getHeight();
            for (float gx = x; gx < x + w; gx += tileW) {
                for (float gy = y; gy < y + h; gy += tileH) {
                    batch.draw(grainTex, gx, gy, tileW, tileH);
                }
            }
            batch.setColor(Color.WHITE);
        }
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
        normalBg.dispose();
        deadBg.dispose();
        grainTex.dispose();
    }
}
