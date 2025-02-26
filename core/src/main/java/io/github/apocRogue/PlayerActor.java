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
    private float gravity = -600f; // Gravity in pixels/sec^2
    private float jumpPower = 600f;
    private boolean isOnGround = false;

    // Movement parameters
    private float acceleration = 2000f;
    private float friction = 0.90f;
    private float maxSpeed = 600f; // clamp on horizontal speed

    // Double-tap dash
    private float doubleTapThreshold = 0.2f;
    private float lastLeftTapTime = 0f;
    private float lastRightTapTime = 0f;
    private float dashSpeed = 800f;
    private float dashDuration = 0.15f;
    private float dashTimer = 0f;
    private boolean isDashing = false;

    // Extra jumps (double-jump, etc.)
    private float extraJumpFinal = 2;
    private float extraJump = 0;

    // Time counter for double-tap detection
    private float timeCounter = 0f;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeCounter += delta; // for double-tap detection
        handleDoubleTapDash();
        handleDashTimer(delta);

        // Keep from going above top
        clampTopOfScreen();

        // Gravity
        velocityY += gravity * delta;

        if (!isDashing) {
            handleHorizontalMovement(delta);
        }

        // Jump input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }

        // Update position
        setX(getX() + velocityX * delta);
        setY(getY() + velocityY * delta);

        // Wrap horizontally if desired
        wrapHorizontal();

        // Check collisions with FloorTile / PlatformTile / HazardTile
        handleTileCollisions(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    // -------------- Movement & Dash --------------

    private void handleHorizontalMovement(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= acceleration * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += acceleration * delta;
        }
        velocityX *= friction;

        // clamp horizontal speed
        if (velocityX > maxSpeed)  velocityX = maxSpeed;
        if (velocityX < -maxSpeed) velocityX = -maxSpeed;
    }

    private void handleDoubleTapDash() {
        // Left double-tap
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (timeCounter - lastLeftTapTime < doubleTapThreshold) {
                startDash(-dashSpeed);
            }
            lastLeftTapTime = timeCounter;
        }
        // Right double-tap
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (timeCounter - lastRightTapTime < doubleTapThreshold) {
                startDash(dashSpeed);
            }
            lastRightTapTime = timeCounter;
        }
    }

    private void startDash(float dashVel) {
        isDashing = true;
        dashTimer = dashDuration;
        velocityX = dashVel;
    }

    private void handleDashTimer(float delta) {
        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                endDash();
            }
        }
    }

    private void endDash() {
        isDashing = false;
        dashTimer = 0f;
    }

    // -------------- Jump & Extra Jumps --------------

    public void jump() {
        if (isOnGround || extraJump > 0) {
            velocityY = jumpPower;
            if (!isOnGround) {
                // used an extra jump
                extraJump -= 1;
            }
            isOnGround = false;
        }
    }

    // -------------- Collision with Tiles --------------

    private void handleTileCollisions(float delta) {
        if (getStage() == null) return;

        isOnGround = false; // reset each frame

        // Approximate the player's old Y position
        float oldY = getY() - velocityY * delta;

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (overlaps(tile)) {
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();

                        // We only care if we're moving downward
                        if (velocityY <= 0f) {
                            // Where was our bottom last frame?
                            float oldBottom = oldY;
                            // Where is our bottom now?
                            float newBottom = getY();

                            // If we were above tileTop and now below it,
                            // that means we crossed from above in one step.
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                setY(tileTop);
                                velocityY = 0;
                                isOnGround = true;
                                extraJump = extraJumpFinal;
                            }
                        }
                    }
                    else if (tile instanceof HazardTile) {
                        System.out.println("Hit a hazard! (Respawn or lose health)");
                    }
                }
            }
    }

    }

    private boolean overlaps(TileActor tile) {
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle tileRect   = new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        return playerRect.overlaps(tileRect);
    }

    // -------------- Utility / Clamping --------------

    private void clampTopOfScreen() {
        if (getStage() == null) return;
        float topLimit = getStage().getHeight() - getHeight();
        if (getY() > topLimit) {
            setY(topLimit);
            velocityY = 0;
        }
    }

    private void wrapHorizontal() {
        if (getStage() == null) return;
        float stageW = getStage().getWidth();
        if (getX() + getWidth() < 0) {
            setX(stageW);
        }
    }
}
