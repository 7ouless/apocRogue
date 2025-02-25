package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;

public class PlayerActor extends Actor {
    private Texture texture;

    private float velocityY = 0f;  // Vertical velocity
    private float velocityX = 0f;
    private float weight = 1f;
    private float gravity = -600f; // Gravity in pixels/second^2
    private float jumpPower = 600f;
    private boolean isOnGround = false;
    private float acceleration  = 4000f;
    private float friction = 0.90f;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply gravity
        velocityY += gravity * delta;
        setY(getY() + velocityY * delta);
        // Wrap around if we go off the right side
        if (getX() > getStage().getWidth()) {
            setX(-getWidth());
        }

        if (getX() + getWidth() < 0) {
            setX(getStage().getWidth());
        }

        if (getY() > getStage().getHeight()) {
            velocityY = velocityY * 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= acceleration * delta;

        }

        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += acceleration * delta;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }
        velocityX *= (friction * 1);

        setX(getX() + velocityX * delta);

        // Move vertically
        setY(getY() + velocityY * delta);

        // Check collision with the ground
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof FloorActor) {
                if (isCollidingWith((FloorActor) actor)) {
                    // If colliding, place the player on top of the ground and reset vertical velocity
                    setY(actor.getY() + actor.getHeight());
                    velocityY = 0;
                    isOnGround = true;
                }
            }
        }
    }
    public void jump(){
        if (isOnGround) {
            velocityY = jumpPower;
            isOnGround = false;
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
