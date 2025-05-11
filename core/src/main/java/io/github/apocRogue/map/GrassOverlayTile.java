package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.badlogic.gdx.math.Rectangle;


public class GrassOverlayTile extends TileActor {
    private static Texture grass1, grassEdge1, grassEdge2;

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


    public static void loadForRadiation(String folder) {
        if (grass1     != null) grass1.dispose();
        if (grassEdge1 != null) grassEdge1.dispose();
        if (grassEdge2 != null) grassEdge2.dispose();
        grass1     = new Texture("ui/"+folder+"/floor-grass.png");
        grassEdge1 = new Texture("ui/"+folder+"/edge-grass.png");
        grassEdge2 = new Texture("ui/"+folder+"/edge2-grass.png");
    }

}

