package io.github.apocRogue.actors.mapEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Door extends Actor {
    public enum Type { CONTINUE, EXTRACT }

    // load once, like FloorTile.VARIANTS
    private static final Texture CONTINUE_TEX =
        new Texture(Gdx.files.internal("ui/continue-door.png"));
    private static final Texture EXTRACT_TEX  =
        new Texture(Gdx.files.internal("ui/exit-door.png"));

    private final Type type;
    private final Texture texture;
    private final Rectangle bounds;

    public Door(Type type, Vector2 pos) {
        this.type    = type;
        this.texture = (type == Type.CONTINUE) ? CONTINUE_TEX : EXTRACT_TEX;
        setPosition(pos.x, pos.y);
        setSize(texture.getWidth(), texture.getHeight());
        bounds = new Rectangle(pos.x, pos.y, getWidth(), getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY());
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

    /** Call once at app shutdown to free the static textures. */
    public static void disposeTextures() {
        CONTINUE_TEX.dispose();
        EXTRACT_TEX.dispose();
    }
}
