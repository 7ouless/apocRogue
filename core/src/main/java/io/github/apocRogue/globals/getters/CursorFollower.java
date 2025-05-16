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
        //sizing actor to texture size
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        //get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        Vector2 stageCoords = getStage().screenToStageCoordinates(new Vector2(mouseX, mouseY));

        setPosition(stageCoords.x - getWidth() / 2f, stageCoords.y - getHeight() / 2f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
