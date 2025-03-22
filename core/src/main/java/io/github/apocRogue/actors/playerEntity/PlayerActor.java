package io.github.apocRogue.actors.playerEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.useClasses.ItemActor;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.inventory.Inventory;
import io.github.apocRogue.map.*;

// A player character that can move, jump, dash, and have stats
public class PlayerActor extends Actor {

    private Texture texture;

    // Movement & physics fields...
    private float velocityX = 0f;
    private float velocityY = 0f;
    private float gravity   = -600f;
    private float jumpPower = 900f;
    private boolean isOnGround = false;

    // For demonstration, we unify maxSpeed with Stats if you want
    private float friction = 0.95f;

    // Double-tap dash
    private float dashSpeed    = 1500f;
    private float dashDuration = 0.15f;
    private float dashTimer    = 0f;
    private boolean isDashing  = false;

    // Extra jumps
    private float extraJumpFinal = 10000;
    private float extraJump = 0;
    private boolean facingRight = true;

    // For double-tap detection
    private float timeCounter = 0f;

    // The Player's Stats
    private StatsComponent stats;

    // Inventory reference
    private Inventory inventory;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());

        // Example: initialize your stats with some values
        // (health=100, maxHealth=100, strength=10, defense=2, speed=600, dashes=2, jumps=2)
        stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10);
        extraJumpFinal = stats.getJumps();
        // If you want to unify your old 'maxSpeed' with stats:
        // float speedFromStats = stats.getSpeed();
        // Now you can read 'speedFromStats' whenever you do horizontal movement
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeCounter += delta;
        handleDoubleTapDash();
        handleDashTimer(delta);
        handleChestInteraction();
        handleItemPickups();

        // Gravity
        velocityY += gravity * delta;

        // If not dashing, handle normal horizontal movement
        if (!isDashing) {
            handleHorizontalMovement(delta);
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }
        float oldX = getX();
        float oldY = getY();
        // Update position
        setX(getX() + velocityX * delta);
        setY(getY() + velocityY * delta);

        // E.g., clamp top or wrap horizontally
        clampTopOfScreen();
        wrapHorizontal();

        // Collisions
        handleTileCollisions(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    // Example of a new method to apply damage using stats
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Player took " + amount + " damage! Health now: " + stats.getHealth());
        if (stats.isDead()) {
            // e.g. do something on death
            System.out.println("Player died!");
            remove();
        }
    }

    // -------------- Movement & Dash --------------

    private void handleHorizontalMovement(float delta) {
        boolean movingLeft  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        // If you want to unify with stats, read from stats.getSpeed()
        float speed = stats.getSpeed(); // e.g. 600
        if (movingLeft) {
            velocityX -= speed * delta;
            facingRight = false;
        }
        if (movingRight) {
            velocityX += speed * delta;
            facingRight = true;
        }

        velocityX *= friction;
        // clamp horizontal speed
        if (velocityX > speed)  velocityX = speed;
        if (velocityX < -speed) velocityX = -speed;
    }

    private void handleDoubleTapDash() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            startDash(-dashSpeed);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            startDash(dashSpeed);
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
                    if (tile instanceof FloorTile || tile instanceof PlatformTile || tile instanceof BorderTile) {
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
                            if (oldX >= tileRight && playerLeft < tileRight && getY() != tile.getY() + tile.getHeight()) {
                                setX(tileRight);
                                velocityX = 0;
                            }
                        }
                        // If moving right and player's right edge crosses tile's left edge
                        else if (velocityX > 0) {
                            if (oldX + getWidth() <= tileLeft && playerRight > tileLeft && getY() != tile.getY() + tile.getHeight()) {
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
        float topLimit = MapManager.settings.roomHeight - getHeight();
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

    // -------------- Items & Chests --------------

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
        // If user pressed R this frame:
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Check if near any chest
            float interactRange = 80f; // or use the chest’s own range

            // Loop through stage actors to find chests
            for (Actor actor : getStage().getActors()) {
                if (actor instanceof ChestActor) {
                    ChestActor chest = (ChestActor) actor;
                    // if chest is not opened, check distance
                    if (!chest.isOpened()) {
                        float dx = (getX() + getWidth()/2f) - (chest.getX() + chest.getWidth()/2f);
                        float dy = (getY() + getHeight()/2f) - (chest.getY() + chest.getHeight()/2f);
                        float dist2 = dx*dx + dy*dy;

                        if (dist2 < interactRange * interactRange) {
                            // We are close enough to open
                            chest.openByInteraction();
                            System.out.println("Chest opened!");
                            // Optionally break if you only open one chest at a time
                            break;
                        }
                    }
                }
            }
        }
    }
    // If you want a "facingRight" check
    public boolean isFacingRight() {
        return facingRight;
    }
}
