package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;

/**
 * A player character that can move, jump, dash, and collide with tiles.
 */
public class PlayerActor extends Actor {
    private Texture texture;

    // Horizontal/vertical velocities
    private float velocityX = 0f;
    private float velocityY = 0f;

    // Physics
    private float gravity = -600f; // Gravity (pixels/sec^2)
    private float jumpPower = 600f;
    private boolean isOnGround = false;

    // Movement parameters
    private float acceleration = 2000f;
    private float friction = 0.90f;
    private float maxSpeed = 600f; // optional clamp on horizontal speed

    // Double‐tap dash
    private float doubleTapThreshold = 0.2f;
    private float lastLeftTapTime = 0f;
    private float lastRightTapTime = 0f;
    private float dashSpeed = 800f;
    private float dashDuration = 0.15f;
    private float dashTimer = 0f;
    private boolean isDashing = false;

    // Extra jumps (like a double-jump)
    private float extraJumpFinal = 2;
    private float extraJump = 0;

    // A simple time counter for measuring double taps
    private float timeCounter = 0f;

    /**
     * Pass in a valid texture for the player sprite.
     * This sets the actor size to the texture's dimensions.
     */
    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeCounter += delta; // used for double-tap detection

        handleDoubleTapDash();
        handleDashTimer(delta);
        clampTopOfScreen();

        // Apply gravity
        velocityY += gravity * delta;

        if (!isDashing) {
            // Normal horizontal movement
            handleHorizontalMovement(delta);
        }
        // If we are dashing, velocityX was set in startDash(), so skip normal movement.

        // Jump input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }

        // Update position
        setX(getX() + velocityX * delta);
        setY(getY() + velocityY * delta);

        // Optional: Wrap horizontally
        wrapHorizontal();

        // Collisions with tiles
        handleTileCollisions();
    }

    /**
     * Draw the player sprite.
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Start a dash by setting horizontal velocity and marking isDashing = true.
     */
    private void startDash(float dashVel) {
        isDashing = true;
        dashTimer = dashDuration;
        velocityX = dashVel;
    }

    /**
     * End the dash, returning to normal movement logic.
     */
    private void endDash() {
        isDashing = false;
        dashTimer = 0f;
    }

    /**
     * Attempt to jump. If on the ground or have extra jumps, set velocityY to jumpPower.
     */
    public void jump() {
        if (isOnGround || extraJump > 0) {
            velocityY = jumpPower;
            if (!isOnGround) {
                // Using an extra jump in mid-air
                extraJump -= 1;
            }
            isOnGround = false;
        }
    }

    /**
     * Checks bounding-box collision with any FloorTile, PlatformTile, or HazardTile.
     *
     * - If colliding with FloorTile or PlatformTile from above, land on it.
     * - If colliding with HazardTile, handle damage or respawn.
     */
    private void handleTileCollisions() {
        if (getStage() == null) return; // just in case

        isOnGround = false; // We'll reset to true if we land on something

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;

                if (overlaps(tile)) {
                    // If it's ground or platform, land on it
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        // Simple approach: place player on top if we come from above
                        float tileTop = tile.getY() + tile.getHeight();
                        // Check if the player is moving downward
                        if (velocityY <= 0f && getY() >= tileTop) {
                            setY(tileTop);
                            velocityY = 0;
                            isOnGround = true;
                            extraJump = extraJumpFinal;
                        }
                    }
                    else if (tile instanceof HazardTile) {
                        // Hazard collision logic here
                        // For example, reset player position, reduce health, etc.
                        System.out.println("Hit a hazard! Respawn or lose health...");
                    }
                }
            }
        }
    }

    /**
     * Returns true if this player overlaps the bounding rectangle of the given tile.
     */
    private boolean overlaps(TileActor tile) {
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle tileRect = new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        return playerRect.overlaps(tileRect);
    }

    /**
     * Horizontal movement with acceleration and friction.
     */
    private void handleHorizontalMovement(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= acceleration * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += acceleration * delta;
        }

        // Apply friction
        velocityX *= friction;

        // Clamp horizontal speed
        if (velocityX > maxSpeed)  velocityX = maxSpeed;
        if (velocityX < -maxSpeed) velocityX = -maxSpeed;
    }

    /**
     * Detect double-taps for dashing.
     */
    private void handleDoubleTapDash() {
        // Check for left double-tap
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (timeCounter - lastLeftTapTime < doubleTapThreshold) {
                // double tap left
                startDash(-dashSpeed);
            }
            lastLeftTapTime = timeCounter;
        }
        // Check for right double-tap
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (timeCounter - lastRightTapTime < doubleTapThreshold) {
                // double tap right
                startDash(dashSpeed);
            }
            lastRightTapTime = timeCounter;
        }
    }

    /**
     * Counts down dash timer and ends dash if time is up.
     */
    private void handleDashTimer(float delta) {
        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                endDash();
            }
        }
    }

    /**
     * Clamp the player's Y so they can't go above the top of the stage.
     */
    private void clampTopOfScreen() {
        if (getStage() == null) return;
        float topLimit = getStage().getHeight() - getHeight();
        if (getY() > topLimit) {
            setY(topLimit);
            velocityY = 0;
        }
    }

    /**
     * If the player goes off the right edge, wrap to the left, and vice versa.
     */
    private void wrapHorizontal() {
        if (getStage() == null) return;
        float stageW = getStage().getWidth();
        if (getX() > stageW) {
            setX(-getWidth());
        }
        else if (getX() + getWidth() < 0) {
            setX(stageW);
        }
    }
}
