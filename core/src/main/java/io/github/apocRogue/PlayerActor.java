package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;

public class PlayerActor extends Actor {
    private Texture texture;

    // Horizontal/vertical velocities
    private float velocityX = 0f;
    private float velocityY = 0f;

    // Physics
    private float gravity = -600f;   // Gravity in pixels/second^2
    private float jumpPower = 600f;
    private boolean isOnGround = false;

    // Movement parameters
    private float acceleration = 2000f;
    private float friction = 0.90f;
    private float maxSpeed = 600f; // optional clamp if you want a top speed

    // Double‐tap dash settings
    private float doubleTapThreshold = 0.2f;  // Max time between taps
    private float lastLeftTapTime = 0f;       // When user last tapped left
    private float lastRightTapTime = 0f;      // When user last tapped right
    private float dashSpeed = 800f;           // Velocity to set when dashing
    private float dashDuration = 0.15f;       // How long the dash lasts (seconds)
    private float dashTimer = 0f;            // Counts down once we start a dash
    private boolean isDashing = false;

    // We'll keep track of time in the actor; you could also track in the Screen.
    private float timeCounter = 0f;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Keep a running time so we can measure intervals between taps
        timeCounter += delta;

        // ----------------------------
        // 1. Detect Double‐Tap
        // ----------------------------
        // If the user JUST pressed left:
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            // Check how long since last left tap
            if (timeCounter - lastLeftTapTime < doubleTapThreshold) {
                // Double tap detected, start a dash to the left
                startDash(-dashSpeed);
            }
            lastLeftTapTime = timeCounter;
        }
        // If the user JUST pressed right:
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (timeCounter - lastRightTapTime < doubleTapThreshold) {
                // Double tap detected, dash right
                startDash(dashSpeed);
            }
            lastRightTapTime = timeCounter;
        }

        // ----------------------------
        // 2. Handle Dash Timer
        // ----------------------------
        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                endDash();
            }
        }

        // ----------------------------
        // 3. Apply Gravity
        // ----------------------------
        velocityY += gravity * delta;

        // ----------------------------
        // 4. Horizontal Movement
        // ----------------------------
        if (!isDashing) {
            // Normal movement if not dashing
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                velocityX -= acceleration * delta;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                velocityX += acceleration * delta;
            }
            // Apply friction
            velocityX *= friction;

            // Optional: Clamp horizontal speed
            if (velocityX > maxSpeed)  velocityX = maxSpeed;
            if (velocityX < -maxSpeed) velocityX = -maxSpeed;
        }
        else {
            // If we are dashing, you can skip friction or normal movement
            // because velocityX was set in startDash().
        }

        // ----------------------------
        // 5. Jumping
        // ----------------------------
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }

        // ----------------------------
        // 6. Update Position
        // ----------------------------
        setX(getX() + velocityX * delta);
        setY(getY() + velocityY * delta);

        // Optional: Wrap horizontally
        if (getX() > getStage().getWidth()) {
            setX(-getWidth());
        }
        if (getX() + getWidth() < 0) {
            setX(getStage().getWidth());
        }

        // If we go too high, you do something with velocityY
        if (getY() > getStage().getHeight()) {
            velocityY *= 2;
        }

        // ----------------------------
        // 7. Collision Check
        // ----------------------------
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof FloorActor) {
                if (isCollidingWith((FloorActor) actor)) {
                    setY(actor.getY() + actor.getHeight());
                    velocityY = 0;
                    isOnGround = true;
                }
            }
        }
    }

    /**
     * Start a dash by setting isDashing = true and giving a big horizontal velocity.
     */
    private void startDash(float dashVel) {
        isDashing = true;
        dashTimer = dashDuration;
        velocityX = dashVel;  // instantly set horizontal speed
    }

    /**
     * End the dash, returning to normal movement.
     */
    private void endDash() {
        isDashing = false;
        dashTimer = 0f;
    }

    /**
     * Jump only if on ground.
     */
    public void jump() {
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
     * Simple bounding rectangle collision with a FloorActor.
     */
    private boolean isCollidingWith(FloorActor ground) {
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle groundRect = new Rectangle(
            ground.getX(), ground.getY(), ground.getWidth(), ground.getHeight()
        );
        return playerRect.overlaps(groundRect);
    }
}
