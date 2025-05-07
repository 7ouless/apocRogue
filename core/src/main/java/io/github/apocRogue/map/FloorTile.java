package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class FloorTile extends TileActor {
    private static final Texture tex = new Texture("ui/floor.png");

    public FloorTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.GREEN);   // ← call the TileActor ctor
        // (we won’t actually use the Color fill, since we override draw below)
        setSize(width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(tex, getX(), getY(), getWidth(), getHeight());
    }
}
