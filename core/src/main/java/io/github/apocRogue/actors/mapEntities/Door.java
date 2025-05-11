package io.github.apocRogue.actors.mapEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


public class Door extends Actor {
    public enum Type { CONTINUE, EXTRACT }

    private static final float SCALE = 0.08f;

    private final int radiationLevel;

    private static final Texture CONTINUE_TEX =
        new Texture(Gdx.files.internal("ui/continue-door.png"));
    private static final Texture EXTRACT_TEX  =
        new Texture(Gdx.files.internal("ui/exit-door.png"));

    private final Type type;
    private final Texture texture;
    private final Rectangle bounds;

    private static final BitmapFont FONT = new BitmapFont();
    private static final GlyphLayout LAYOUT = new GlyphLayout();


    public Door(Type type, Vector2 pos,int radiationLevel) {
        this.type    = type;
        this.radiationLevel = radiationLevel;
        this.texture = (type == Type.CONTINUE) ? CONTINUE_TEX : EXTRACT_TEX;

        setPosition(pos.x, pos.y);
        setSize(texture.getWidth() * SCALE, texture.getHeight() * SCALE);

        bounds = new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

    public Door(Type type, Vector2 pos) {
        this(type, pos, 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // tint by door type
        Color tint = (type == Type.CONTINUE)
            ? Color.RED
            : Color.BLUE;
        batch.setColor(tint);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        batch.setColor(Color.WHITE); // restore

        if (type == Type.CONTINUE) {
            // how wide is the number?
            String text = String.valueOf(radiationLevel);
            LAYOUT.setText(FONT, text);
            float textX = getX() + (getWidth()  - LAYOUT.width)  * 0.5f;
            float textY = getY() + getHeight() + LAYOUT.height + 4;
            // draw in white so it stands out on red
            FONT.setColor(Color.WHITE);
            FONT.draw(batch, LAYOUT, textX, textY);
        }

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        bounds.setPosition(getX(), getY());
    }

    public Type getType() {
        return type;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public static void disposeTextures() {
        CONTINUE_TEX.dispose();
        EXTRACT_TEX.dispose();
        FONT.dispose();
    }

    public int getRadiationLevel() { return radiationLevel; }
}
