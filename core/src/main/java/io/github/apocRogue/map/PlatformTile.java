package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class PlatformTile extends TileActor {
    private static final Texture TEX = new Texture("ui/platform.png");

    public PlatformTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.WHITE);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(TEX,
            Math.round(getX()),
            Math.round(getY()),
            Math.round(getWidth()) + 1,
            Math.round(getHeight()));
    }
}
