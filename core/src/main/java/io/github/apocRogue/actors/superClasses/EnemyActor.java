package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.apocRogue.actorAi.AIBehavior;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;
import com.badlogic.gdx.math.Rectangle;

/**
 * A base class for any AI-driven enemy/mob in your game.
 * It holds common fields like stats, velocity, collision, etc.
 */
public class EnemyActor extends Image {

    protected StatsComponent stats;

    // Movement & physics
    protected float velocityX = 0f;
    protected float velocityY = 0f;
    protected float gravity   = -600f;
    public boolean isOnGround = false;
    protected AIBehavior aiBehavior;

    // Possibly some base jump logic
    protected float jumpCooldown = 2f;
    protected float jumpTimer = 0f;
    protected float jumpPower = 600f;

    public EnemyActor(Texture texture, float x, float y, StatsComponent stats) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());

        this.stats = stats;

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // For example, apply gravity:
        if (!isOnGround) {
            velocityY += gravity * delta;
        }

        float oldX = getX();
        float oldY = getY();

        // Vertical movement
        setY(getY() + velocityY * delta);

        if (getY() < 0) {
            setY(0);
            velocityY = 0;
            isOnGround = true;
        }
        handleTileCollisions(delta, oldX, oldY);

        // Check collision with PlayerActor
        checkCollisionWithPlayer();
        // ...
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }
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

    protected boolean overlaps(TileActor tile) {
        // same as your existing code
        Rectangle mobRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle tileRect = new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        return mobRect.overlaps(tileRect);
    }

    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Enemy took " + amount + " damage! Health now " + stats.getHealth());
        if (stats.isDead()) {
            remove();
        }
    }
    public void jump() {
        if (isOnGround && jumpTimer <= 0f) {
            velocityY = jumpPower;
            isOnGround = false;
            jumpTimer = jumpCooldown;
        }
    }

    private void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        Rectangle hitbox = getBounds();
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                PlayerActor player = (PlayerActor) actor;
                Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
                if (hitbox.overlaps(playerRect)) {
                    System.out.println("hitbox collided with the player!");
                    // Possibly do damage to player or dummy, etc.
                }
            }
        }
    }
    // Possibly getters for velocity, stats, etc.
    // ...
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
    public void setAIBehavior(AIBehavior ai) {
        this.aiBehavior = ai;
    }

    public AIBehavior getAIBehavior() {
        return aiBehavior;
    }

    public StatsComponent getStats() {
        return stats;
    }

    public float getJumpTimer() {
        return jumpTimer;
    }
}
