package io.github.apocRogue;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class ItemActor extends Image {
    private Weapon weapon;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private float gravity = -600f;  // downward acceleration (pixels/sec^2)
    private boolean isOnGround = false;
    private float groundLevel = 0;  // assume y=0 is the floor; adjust as needed

    public ItemActor(Weapon weapon, float x, float y) {
        super(weapon.getTexture());
        this.weapon = weapon;
        setPosition(x, y);
        setSize(weapon.getTexture().getWidth(), weapon.getTexture().getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // If not on the ground, apply gravity
        if (!isOnGround) {
            velocityY += gravity * delta;
            float newY = getY() + velocityY * delta;
            float newX = getX() + velocityX * delta;

            // Simple floor check: if below groundLevel, clamp to ground
            if (newY <= groundLevel) {
                newY = groundLevel;
                velocityY = 0;
                isOnGround = true;
            }
            setPosition(newX, newY);
        }
    }

    // Optionally let other code set velocities
    public void setVelocity(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.isOnGround = false;  // so it will start falling again
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
