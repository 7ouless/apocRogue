package io.github.apocRogue.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.inventory.Inventory;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.HazardTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

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
    private float extraJumpFinal = 10000;
    private float extraJump = 0;
    private boolean facingRight = true; // default facing right

    // Time counter for double-tap detection
    private float timeCounter = 0f;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
    }
    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    public Inventory getInventory() {
        return inventory;
    }
    @Override
    public void act(float delta) {
        super.act(delta);

        timeCounter += delta; // for double-tap detection
        handleDoubleTapDash();
        handleDashTimer(delta);
        handleChestInteraction();

        handleItemPickups();

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
        boolean movingLeft  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (movingLeft) {
            velocityX -= acceleration * delta;
            facingRight = false; // We are facing left
        }
        if (movingRight) {
            velocityX += acceleration * delta;
            facingRight = true; // We are facing right
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

        // Compute "old" positions based on current velocities
        float oldX = getX() - velocityX * delta;
        float oldY = getY() - velocityY * delta;

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (overlaps(tile)) {
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();

                        // Vertical collision: if moving downward and crossing the tile's top
                        if (velocityY <= 0f) {
                            float oldBottom = oldY;
                            float newBottom = getY();
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                setY(tileTop);
                                velocityY = 0;
                                isOnGround = true;
                                extraJump = extraJumpFinal;
                            }
                        }

                        // Horizontal collision:
                        float tileLeft = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float playerLeft = getX();
                        float playerRight = getX() + getWidth();

                        // If moving left and player's left edge crosses tile's right edge
                        if (velocityX < 0) {
                            if (oldX >= tileRight && playerLeft < tileRight) {
                                setX(tileRight);
                                velocityX = 0;
                            }
                        }
                        // If moving right and player's right edge crosses tile's left edge
                        else if (velocityX > 0) {
                            if (oldX + getWidth() <= tileLeft && playerRight > tileLeft) {
                                setX(tileLeft - getWidth());
                                velocityX = 0;
                            }
                        }
                    } else if (tile instanceof HazardTile) {
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
    public boolean isFacingRight(){
        return facingRight;
    }
    private void handleItemPickups() {
        if (getStage() == null) return;

        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Array<Actor> toRemove = new Array<>();

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof ItemActor) {
                ItemActor item = (ItemActor) actor;
                if (playerRect.overlaps(item.getBounds())) {
                    boolean success = getInventory().addItem(item.getWeapon());
                    if (success) {
                        // Inventory accepted the item
                        toRemove.add(item);
                    } else {
                        // Inventory is full; drop it from the player
                        // Place it near the player's center
                        float dropX = getX() + getWidth() / 2f - item.getWidth() / 2f;
                        float dropY = getY() + getHeight() / 2f;
                        item.setPosition(dropX, dropY);

                        // Give it a little upward + sideways velocity
                        float horizontalPush = isFacingRight() ? 100f : -100f;
                        item.setVelocity(horizontalPush, 200f);
                    }
                }
            }
        }

        // Remove picked-up items
        for (Actor a : toRemove) {
            a.remove();
        }
    }
    private void handleChestInteraction() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // Check if near any chest
            float interactRange = 200f;
            for (Actor actor : getStage().getActors()) {
                if (actor instanceof ChestActor) {
                    ChestActor chest = (ChestActor) actor;
                    if (!chest.isOpened()) {
                        float dx = (getX() + getWidth()/2f) - (chest.getX() + chest.getWidth()/2f);
                        float dy = (getY() + getHeight()/2f) - (chest.getY() + chest.getHeight()/2f);
                        float dist2 = dx*dx + dy*dy;
                        if (dist2 < interactRange*interactRange) {
                            chest.openByInteraction();
                            System.out.println("Chest opened!");
                        }
                    }
                }
            }
        }
    }

}
