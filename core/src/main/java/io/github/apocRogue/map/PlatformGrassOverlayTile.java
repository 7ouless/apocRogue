package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class PlatformGrassOverlayTile extends TileActor {
    private static Texture grassTex;
    private static final float Y_OFFSET = 20f;
    private final Texture texture;

    public PlatformGrassOverlayTile(float x, float y, float width, float height) {
        super(x, y, width, height, null);
        setSize(width, height);
        this.texture = grassTex;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        toFront();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float pad = 0.01f;
        float drawX = Math.round(getX());
        float drawW = getWidth();
        // flip texture vertically by swapping the v coordinates
        float u = pad;
        float v = 1 - pad;
        float u2 = 1 - pad;
        float v2 = pad;
        batch.draw(
            texture,
            drawX, getY() + Y_OFFSET,
            drawW, getHeight(),
            u, v, u2, v2
        );
    }

    public static void loadForRadiation(String folder) {
        if (grassTex != null) {
            grassTex.dispose();
        }
        grassTex = new Texture("ui/" + folder + "/platform-grass.png");
    }
}
