package io.github.apocRogue.globals.getters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CursorFollower extends Actor {
    private Texture texture;

    public CursorFollower(Texture texture) {
        this.texture = texture;
        // Set the size of the actor to match the texture dimensions
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Get the current mouse position in screen coordinates
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Convert screen coordinates to stage coordinates.
        // This assumes the actor is added to a Stage.
        Vector2 stageCoords = getStage().screenToStageCoordinates(new Vector2(mouseX, mouseY));

        // Optionally center the actor on the cursor:
        setPosition(stageCoords.x - getWidth() / 2f, stageCoords.y - getHeight() / 2f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw the texture at the current position
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
