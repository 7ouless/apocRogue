package io.github.apocRogue;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;

public class PlayerActor extends Actor {
    private Texture texture;

    private float speedX = 100f;   // Horizontal speed
    private float velocityY = 0f;  // Vertical velocity
    private float gravity = -300f; // Gravity in pixels/second^2

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply gravity
        velocityY += gravity * delta;

        // Move horizontally
        setX(getX() + speedX * delta);
        // Wrap around if we go off the right side
        if (getX() > getStage().getWidth()) {
            setX(-getWidth());
        }

        // Move vertically
        setY(getY() + velocityY * delta);

        // Check collision with the ground
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof FloorActor) {
                if (isCollidingWith((FloorActor) actor)) {
                    // If colliding, place the player on top of the ground and reset vertical velocity
                    setY(actor.getY() + actor.getHeight());
                    velocityY = 0;
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Simple bounding rectangle collision with a GroundActor.
     */
    private boolean isCollidingWith(FloorActor ground) {
        // Player bounding box
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        // Ground bounding box
        Rectangle groundRect = new Rectangle(
            ground.getX(), ground.getY(), ground.getWidth(), ground.getHeight()
        );
        return playerRect.overlaps(groundRect);
    }
}
