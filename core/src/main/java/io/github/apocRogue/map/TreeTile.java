package io.github.apocRogue.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;

public class TreeTile extends TileActor {
    // load both variants inside the class
    private static final Texture TREE1 = new Texture(Gdx.files.internal("ui/low/tree.png"));
    private static final Texture TREE2 = new Texture(Gdx.files.internal("ui/low/tree2.png"));

    private final boolean flipX;
    private final boolean useAlt;

    // NEW: accept flip & variant flags
    public TreeTile(float x, float y, float width, float height,
                    boolean flipX, boolean useAlt) {
        super(x, y, width, height, null);
        this.flipX = flipX;
        this.useAlt = useAlt;
        // setBounds so the actor's position/size is correct
        setBounds(x, y, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Texture t = useAlt ? TREE2 : TREE1;
        batch.draw(
            t,
            getX(), getY(),
            getWidth(), getHeight(),
            0, 0,
            t.getWidth(), t.getHeight(),
            flipX, false
        );
    }

    @Override
    public Rectangle getBounds() {
        // zero‐sized so you walk right through it
        return new Rectangle(0, 0, 0, 0);
    }
}
