package io.github.apocRogue;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;

public class SlashActor extends Image {
    private float timeAlive = 0f;
    private float maxDuration = 0.2f; // Show slash for 0.2 seconds

    public SlashActor(Texture slashTexture, float x, float y) {
        super(slashTexture);
        setPosition(x, y);
        // Optionally scale or rotate to match player direction
        // setRotation(45f); // for example
        // setScale(1.5f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeAlive += delta;
        // Remove slash after time is up
        if (timeAlive >= maxDuration) {
            remove();
        }
    }
}
