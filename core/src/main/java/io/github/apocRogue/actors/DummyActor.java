package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

public class DummyActor extends Image {
    private int health = 50;

    // Movement & physics
    private float maxSpeed = 60f;      // horizontal speed
    private float velocityY = 0f;
    private float gravity = -600f;
    private boolean isOnGround = false;

    // Jump logic
    private float jumpCooldown = 2f;   // seconds between jumps
    private float jumpTimer = 0f;
    private float jumpPower = 600f;

    public DummyActor(Texture texture, float x, float y) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Save old positions for collision resolution
        float oldX = getX();
        float oldY = getY();

        // Apply gravity if not on the ground
        if (!isOnGround) {
            velocityY += gravity * delta;
        }

        // Horizontal chase toward player
        chasePlayer(delta);

        // Vertical movement
        setY(getY() + velocityY * delta);

        // Simple floor check (y=0 is the floor)
        if (getY() < 0) {
            setY(0);
            velocityY = 0;
            isOnGround = true;
        }

        // Jump logic: occasionally jump if on ground
        jumpTimer -= delta;
        if (isOnGround && jumpTimer <= 0f) {
            jump();
        }

        // Handle collisions with tiles (both vertical and horizontal)
        handleTileCollisions(delta, oldX, oldY);

        // Check collision with PlayerActor (for damage, etc.)
        checkCollisionWithPlayer();
    }

    private void chasePlayer(float delta) {
        // 1. Find the player
        PlayerActor player = findPlayer();
        if (player == null) return;

        // 2. Determine direction toward the player
        float dummyCenterX = getX() + getWidth() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float dx = playerCenterX - dummyCenterX;
        float desiredDirection = Math.signum(dx);
        float moveAmount = desiredDirection * maxSpeed * delta;
        setX(getX() + moveAmount);

        // Optional: clamp to stage boundaries
        if (getStage() != null) {
            float stageWidth = getStage().getWidth();
            if (getX() < 0) {
                setX(0);
            } else if (getX() + getWidth() > stageWidth) {
                setX(stageWidth - getWidth());
            }
        }
    }

    private PlayerActor findPlayer() {
        if (getStage() == null) return null;
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                return (PlayerActor) actor;
            }
        }
        return null;
    }

    private void jump() {
        velocityY = jumpPower;
        isOnGround = false;
        jumpTimer = jumpCooldown;
    }

    private void handleTileCollisions(float delta, float oldX, float oldY) {
        if (getStage() == null) return;
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (overlaps(tile)) {
                    // Handle collisions only for floor/platform tiles
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();

                        // Vertical collision: if falling and crossing the tile's top edge
                        if (velocityY <= 0f) {
                            float oldBottom = oldY;
                            float newBottom = getY();
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                setY(tileTop);
                                velocityY = 0;
                                isOnGround = true;
                            }
                        }

                        // Horizontal collision:
                        float tileLeft = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float currentLeft = getX();
                        float currentRight = getX() + getWidth();

                        // If moving left (oldX > currentX) and crossing tile's right edge:
                        if (oldX > getX() && oldX >= tileRight && currentLeft < tileRight) {
                            setX(tileRight);
                        }
                        // If moving right (oldX < currentX) and crossing tile's left edge:
                        else if (oldX < getX() && oldX + getWidth() <= tileLeft && currentRight > tileLeft) {
                            setX(tileLeft - getWidth());
                        }
                    }
                }
            }
        }
    }

    // Helper method to check if DummyActor overlaps a TileActor
    private boolean overlaps(TileActor tile) {
        Rectangle dummyRect = getBounds();
        Rectangle tileRect = new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        return dummyRect.overlaps(tileRect);
    }

    private void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        Rectangle dummyRect = getBounds();
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                PlayerActor player = (PlayerActor) actor;
                Rectangle playerRect = new Rectangle(
                    player.getX(), player.getY(), player.getWidth(), player.getHeight()
                );
                if (dummyRect.overlaps(playerRect)) {
                    System.out.println("Dummy collided with the player!");
                    // Add additional collision response (damage, knockback, etc.) here if desired.
                }
            }
        }
    }

    public void takeDamage(int amount) {
        health -= amount;
        System.out.println("Dummy took " + amount + " damage! Health now " + health);
        if (health <= 0) {
            remove();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
