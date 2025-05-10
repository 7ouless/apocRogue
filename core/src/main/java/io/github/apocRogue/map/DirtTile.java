package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;


public class DirtTile extends TileActor {
    private static final Texture[] VARIANTS = {
        new Texture("ui/floor.png"),
        new Texture("ui/floor2.png"),
        new Texture("ui/floor3.png")
    };
    private final boolean flipX;
    private final Texture tex;


    public DirtTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.BROWN);
        this.flipX = MathUtils.randomBoolean();  // random flip
        this.tex    = VARIANTS[ MathUtils.random(VARIANTS.length - 1) ];
        setSize(width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float pad = 0.01f; // prevent bleeding
        float drawX = getX();
        float drawW = getWidth();
        if (flipX) {
            drawX += drawW;
            drawW = -drawW;
        }
        // flipped negative-width still respects UV pad parameters
        batch.draw(
            tex,
            drawX, getY(), drawW, getHeight(),
            pad, pad, 1 - pad, 1 - pad
        );
    }
}



