package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class EdgeTile extends TileActor {
    private static final Texture tex = new Texture("ui/edge.png");

    public EdgeTile(float x, float y, float size) {
        super(x, y, size, size, Color.PURPLE);
        setSize(size, size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(tex, getX(), getY(), getWidth(), getHeight());
    }


}
