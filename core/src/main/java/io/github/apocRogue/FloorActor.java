package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;

public class FloorActor extends Actor {
    // You could also draw a texture here; for simplicity, we’ll just draw a colored rectangle.
    private Color groundColor = Color.BROWN;

    public FloorActor(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Save the old color
        Color oldColor = batch.getColor();
        Texture whiteTexture = new Texture(Gdx.files.internal("ui/whitepixel.jpg"));

        // Set the batch color to groundColor, then draw a rectangle
        batch.setColor(groundColor.r, groundColor.g, groundColor.b, groundColor.a * parentAlpha);
        batch.draw(
            whiteTexture, getX(), getY(), getWidth(), getHeight()
        );

        // Restore the old color
        batch.setColor(oldColor);
    }
}
