package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public abstract class PhysicalActor extends Image {
    public float velocityX = 0f;
    public float velocityY = 0f;
    public boolean isOnGround = false;

    public PhysicalActor(Texture texture) {
        super(texture); // This calls the Image(Texture) constructor.
    }

    public abstract void takeDamage(int amount);

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
