package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.badlogic.gdx.math.Rectangle;


public class GrassOverlayTile extends TileActor {
    private static final Texture grass1 = new Texture("ui/low/floor-grass.png");
    private static final Texture grassEdge1 = new Texture("ui/low/edge-grass.png");
    private static final Texture grassEdge2 = new Texture("ui/low/edge2-grass.png");

    private final Texture texture;
    private final boolean flipX;

    public GrassOverlayTile(float x, float y, float width, float height, boolean flipX, String type) {
        super(x, y, width, height, null);
        this.flipX = flipX;
        setSize(width, height);

        switch (type) {
            case "edge1": texture = grassEdge1; break;
            case "edge2": texture = grassEdge2; break;
            default: texture = grass1; break;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX();
        float drawW = getWidth();
        if (flipX) {
            drawX += drawW;
            drawW = -drawW;
        }
        batch.draw(texture, drawX, getY(), drawW, getHeight());
    }

    @Override
    public com.badlogic.gdx.math.Rectangle getBounds() {
        return new com.badlogic.gdx.math.Rectangle(0, 0, 0, 0);
    }

}

