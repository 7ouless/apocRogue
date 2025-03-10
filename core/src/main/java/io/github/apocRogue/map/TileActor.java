package io.github.apocRogue.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;

public abstract class TileActor extends Actor {
    protected Color color;
    private static final Texture WHITE_PIXEL =
        new Texture(Gdx.files.internal("ui/whitepixel.jpg"));

    public TileActor(float x, float y, float width, float height, Color color) {
        this.color = color;
        setBounds(x, y, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color oldColor = batch.getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(WHITE_PIXEL, getX(), getY(), getWidth(), getHeight());
        batch.setColor(oldColor);
    }
}
