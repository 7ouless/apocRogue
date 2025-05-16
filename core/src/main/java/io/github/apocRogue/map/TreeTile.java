package io.github.apocRogue.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import io.github.apocRogue.globals.difficulty.CurrentDificulty;

public class TreeTile extends TileActor {
    // load both variants inside the class
    private static Texture TREE1, TREE2;

    private final boolean flipX;
    private final boolean useAlt;


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

        float yPos = getY();
        if (CurrentDificulty.getRadiation() == 3) {
            yPos -= 10f;    //
        }

        batch.draw(
            t,
            getX(), yPos,
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

    public static void loadForRadiation(String folder) {
        if (TREE1 != null) TREE1.dispose();
        if (TREE2 != null) TREE2.dispose();
        TREE1 = new Texture(Gdx.files.internal("ui/"+folder+"/tree.png"));
        TREE2 = new Texture(Gdx.files.internal("ui/"+folder+"/tree2.png"));
    }

}
