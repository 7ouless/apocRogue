package io.github.apocRogue;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;

public class ArrowActor extends Image {
    private float speed = 300f; // pixels per second
    // Optionally track direction if your player can face left or right
    private float direction = 1f; // +1 for right, -1 for left

    public ArrowActor(Texture arrowTexture, float x, float y, float direction) {
        super(arrowTexture);
        setPosition(x, y);
        this.direction = direction;
        // If you have an arrow image, rotate or flip if needed
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Move horizontally
        setX(getX() + speed * direction * delta);

        // Remove if it goes off-screen (assuming 1080 width, for example)
        if (getX() < -50 || getX() > 1130) {
            remove();
        }
    }
}
