package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class EdgeTile extends TileActor {
    private static final Texture tex = new Texture("ui/edge.png");
    private boolean flipX;

    public EdgeTile(float x, float y, float size, boolean flipX) {
        super(x, y, size, size, Color.PURPLE);
        this.flipX = flipX;
        setSize(size, size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX();
        float drawW = getWidth();

        if (flipX) {
            drawX += getWidth();    // draw from the right
            drawW = -drawW;         // flip horizontally
        }

        batch.draw(tex, drawX, getY(), drawW, getHeight());
    }
}
