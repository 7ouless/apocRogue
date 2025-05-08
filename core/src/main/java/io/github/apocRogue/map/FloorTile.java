package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;


public class FloorTile extends TileActor {
    private static final Texture tex = new Texture("ui/floor.png");
    private final boolean flipX;     // ← new!

    public FloorTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.WHITE);
        this.flipX = MathUtils.randomBoolean();  // random flip
        setSize(width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX();
        float drawW = getWidth();
        if (flipX) {
            // shift origin right, then draw negative width
            drawX += drawW;
            drawW = -drawW;
        }
        batch.draw(tex, drawX, getY(), drawW, getHeight());
    }
}

