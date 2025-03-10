package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;

public class DummyActor extends Image {
    private int health = 50;

    // Movement & physics
    private float maxSpeed = 60f;      // horizontal speed
    private float velocityY = 0f;
    private float gravity = -600f;
    private boolean isOnGround = false;

    // Jump logic
    private float jumpCooldown = 2f;   // time (seconds) between jumps
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

        // Apply gravity if not on the ground
        if (!isOnGround) {
            velocityY += gravity * delta;
        }

        // Horizontal chase
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

        // Check collision with PlayerActor
        checkCollisionWithPlayer();
    }

    private void chasePlayer(float delta) {
        // 1. Find the player
        PlayerActor player = findPlayer();
        if (player == null) return;

        // 2. Determine which way to move
        float dummyCenterX = getX() + getWidth() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float dx = playerCenterX - dummyCenterX;

        // 3. Move horizontally toward the player
        //    If dx > 0, player is to the right; dx < 0, player is to the left
        float desiredDirection = Math.signum(dx); // +1 if player is right, -1 if left, 0 if same X
        float moveAmount = desiredDirection * maxSpeed * delta;

        setX(getX() + moveAmount);

        // (Optional) If you want to clamp to stage boundaries:
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

    private void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        Rectangle dummyRect = getBounds();

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                PlayerActor player = (PlayerActor) actor;
                Rectangle playerRect = new Rectangle(
                    player.getX(), player.getY(),
                    player.getWidth(), player.getHeight()
                );
                if (dummyRect.overlaps(playerRect)) {
                    System.out.println("Dummy collided with the player!");
                    // e.g., deal damage or push the player
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
