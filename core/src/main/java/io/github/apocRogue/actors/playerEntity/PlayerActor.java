package io.github.apocRogue.actors.playerEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.useClasses.ItemActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.globals.movementProcesses.StepUpProcessor;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.map.*;
import io.github.apocRogue.globals.physics.GravitySystem;
import com.badlogic.gdx.scenes.scene2d.Actor;
// ... other imports

public class PlayerActor extends PhysicalActor {

    private Texture texture;
    private static final float STEP_HEIGHT = 32f;

    private float jumpPower = 900f;
    private float friction  = 0.95f;
    private boolean facingRight = true;

    // For double-tap dash:
    private float dashSpeed    = 1500f;
    private float dashDuration = 0.15f;
    private float dashTimer    = 0f;
    private boolean isDashing  = false;
    private Inventory inventory;  // store a reference to the player's Inventory
    // For double-tap detection
    private float timeCounter = 0f;

    // The Player's Stats
    // Example Stats
    private StatsComponent stats;

    public PlayerActor(Texture texture) {
        super(texture);
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());

        // Example stats
        stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10, 0);
    }

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Damage Taken" + amount);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply physics via GravitySystem with factor=1f for normal gravity:
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
        timeCounter += delta;
        StepUpProcessor.attemptStepUp(this);

        handleChestInteraction();
        handleItemPickups();
        // Apply friction if not dashing:
        if (!isDashing) {
            velocityX *= friction;
        }

        // Horizontal input (left-right) and dash logic:
        handleHorizontalMovement(delta);
        handleDash(delta);

        // Jump input:
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }

        // Move horizontally (GravitySystem handled vertical in applyGravityAndPhysics)
        setX(getX() + velocityX * delta);


    }

    private void handleHorizontalMovement(float delta) {
        boolean movingLeft  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        float speed = stats.getSpeed(); // e.g. 600
        if (movingLeft) {
            velocityX -= speed * delta;
            facingRight = false;
        }
        if (movingRight) {
            velocityX += speed * delta;
            facingRight = true;
        }

        // Optionally clamp horizontal speed if you like:
        if (velocityX > speed)  velocityX = speed;
        if (velocityX < -speed) velocityX = -speed;
    }

    private void handleDash(float delta) {
        // Example dash logic:
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            startDash(-dashSpeed);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            startDash(dashSpeed);
        }

        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                endDash();
            }
        }
    }

    private void startDash(float dashVel) {
        isDashing = true;
        dashTimer = dashDuration;
        velocityX = dashVel;
    }

    private void endDash() {
        isDashing = false;
        dashTimer = 0f;
    }

    public void jump() {
        if (isOnGround) {
            velocityY = jumpPower;
            isOnGround = false;
        }
    }

    private boolean overlapsHorizontally(TileActor tile) {
        float playerLeft = getX();
        float playerRight = getX() + getWidth();
        float tileLeft = tile.getX();
        float tileRight = tile.getX() + tile.getWidth();

        // If there's any horizontal overlap
        return (playerRight > tileLeft && playerLeft < tileRight);
    }

    private static final float TILE_SIZE = ObstacleGetters.getStandardTileSize();

    private int getPlayerTileX() {
        return (int)((getX() + getWidth() / 2f) / TILE_SIZE);
    }

    private int getPlayerTileY() {
        return (int)(getY() / TILE_SIZE);
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    // If you have logic for collisions, you can either keep that in GravitySystem
    // or handle them separately here. Up to you. For example, if you want advanced collisions:
    // private void handleTileCollisions(float delta) { ... }

    public StatsComponent getStats() {
        return stats;
    }

    public boolean isFacingRight() {
        return facingRight;
    }
    public boolean isPlayerDead() {
        return stats.isDead();
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
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
}

