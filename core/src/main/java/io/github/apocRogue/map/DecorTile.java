package io.github.apocRogue.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;

public class DecorTile extends TileActor {
    private static Texture[] VARIANTS;

    private final boolean flipX;
    private final int     variantIdx;

    public int getVariantIdx() {
        return variantIdx;
    }

    public DecorTile(float x, float y, float width, float height,
                     boolean flipX, int variantIdx) {
        super(x, y, width, height, null);
        this.flipX      = flipX;
        this.variantIdx = variantIdx % VARIANTS.length;
        setBounds(x, y, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Texture tex = VARIANTS[variantIdx];
        batch.draw(
            tex, getX(), getY(),
            getWidth(), getHeight(),
            0, 0, tex.getWidth(), tex.getHeight(),
            flipX, false
        );
    }

    @Override
    public Rectangle getBounds() {
        // zero size => non-collidable like trees
        return new Rectangle(0, 0, 0, 0);
    }

    public static void loadForRadiation(String folder) {
        if (VARIANTS != null) {
            for (Texture t : VARIANTS) t.dispose();
        }
        VARIANTS = new Texture[] {
            new Texture(Gdx.files.internal("ui/"+folder+"/rock.png")),
            new Texture(Gdx.files.internal("ui/"+folder+"/stone.png")),
            new Texture(Gdx.files.internal("ui/"+folder+"/bush.png"))
        };
    }

}
