package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class PlatformEdgeTile extends TileActor {
    private static final Texture tex1 = new Texture("ui/platform-edge.png");
    private static final Texture tex2 = new Texture("ui/platform-edge2.png");

    private final boolean flipX;
    private final Texture texture;

    public PlatformEdgeTile(float x, float y, float size, boolean flipX, boolean useAltTexture) {
        super(x, y, size, size, Color.WHITE);
        this.flipX     = flipX;
        this.texture   = useAltTexture ? tex2 : tex1;
        setSize(size, size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX(), drawW = getWidth();
        if (flipX) {
            drawX += drawW;
            drawW = -drawW;
        }
        batch.draw(texture, drawX, getY(), drawW, getHeight());
    }
}
