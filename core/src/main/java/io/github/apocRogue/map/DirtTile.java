package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class DirtTile extends TileActor {
    private static final Texture tex = new Texture("ui/floor.png");

    public DirtTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.BROWN);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float pad = 0.01f; // tiny inset to prevent bleeding
        batch.draw(
            tex,
            getX(), getY(), getWidth(), getHeight(),
            pad, pad, 1 - pad, 1 - pad
        );
    }

}


