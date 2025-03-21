package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.apocRogue.actorAi.lineOfSight;
import io.github.apocRogue.globals.stats.StatsComponent;  // <--- import your StatsComponent
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

public class DummyActor extends Image {

    // Remove "private int health = 50;"
    // Instead, store a StatsComponent
    private StatsComponent stats;

    // Movement & physics
    private float velocityY = 0f;
    private float gravity = -600f;
    private boolean isOnGround = false;
    private boolean lockedOn = false;
    // Jump logic
    private float jumpCooldown = 2f;   // seconds between jumps
    private float jumpTimer = 0f;
    private float jumpPower = 600f;
    private float lockOnTimerFinal = 10f;
    private float lockOnTimer = lockOnTimerFinal;

    // We can also store or retrieve speed from stats if we want
    private float maxSpeed; // read from stats?

    public DummyActor(Texture texture, float x, float y) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());

        // Initialize stats (health, maxHealth, strength, defense, speed, dashes, jumps)
        // Fill in values that make sense for your dummy
        stats = new StatsComponent(
            50,  // health
            50,  // maxHealth
            5,   // strength
            0,   // defense
            60,  // speed
            0,   // dashes
            1,    // jumps
            1000    // sight sens
        );

        // If you want to unify speed with stats, you can read it here
        this.maxSpeed = stats.getSpeed(); // if you have a getSpeed() returning a float or int
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        float oldX = getX();
        float oldY = getY();

        // Gravity
        if (!isOnGround) {
            velocityY += gravity * delta;
        }

        // Horizontal chase
        chasePlayer(delta);

        // Vertical movement
        setY(getY() + velocityY * delta);

        // Floor check
        if (getY() < 0) {
            setY(0);
            velocityY = 0;
            isOnGround = true;
        }

        // Jump logic


        // Tile collisions
        handleTileCollisions(delta, oldX, oldY);

        // Check collision with PlayerActor
        checkCollisionWithPlayer();
    }

    private void chasePlayer(float delta) {
        PlayerActor player = findPlayer();
        if (player == null) return;

        // If can't see the player, do nothing
        if (lineOfSight.canSeeTarget(this, player, stats.sightSens(), getStage())) {
            lockedOn = true;
        } else{
            lockOnTimer = lockOnTimer - 1;
            if (lockOnTimer < 0){
                lockedOn = false;
                lockOnTimer = lockOnTimerFinal;
            }
        }


        if(lockedOn) {
            // Otherwise, we see the player => do chase or attack
            float dummyCenterX = getX() + getWidth() / 2f;
            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float dx = playerCenterX - dummyCenterX;
            float desiredDirection = Math.signum(dx);
            if (isOnGround && jumpTimer <= 0f) {
                jump();
            }
            jumpTimer -= delta;

            // Move horizontally
            float moveAmount = stats.getSpeed() * desiredDirection * delta;
            setX(getX() + moveAmount);

            // Optional boundary clamp
            if (getStage() != null) {
                float stageWidth = getStage().getWidth();
                if (getX() < 0) {
                    setX(0);
                } else if (getX() + getWidth() > stageWidth) {
                    setX(stageWidth - getWidth());
                }
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
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();
                        System.out.println("Collision with tile at x=" + tile.getX()
                            + " width=" + tile.getWidth()
                            + " => setting X to " + (tile.getX() - getWidth()));
                        // Vertical collision if falling
                        if (velocityY <= 0f) {
                            float oldBottom = oldY;
                            float newBottom = getY();
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                setY(tileTop);
                                velocityY = 0;
                                isOnGround = true;
                            }
                        }

                        // Horizontal collision
                        float tileLeft = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float currentLeft = getX();
                        float currentRight = getX() + getWidth();

                        if (oldX > getX() && oldX >= tileRight && currentLeft < tileRight) {
                            setX(tileRight);
                        } else if (oldX < getX() && oldX + getWidth() <= tileLeft && currentRight > tileLeft) {
                            setX(tileLeft - getWidth());
                        }
                    }
                }
            }
        }
    }

    private boolean overlaps(TileActor tile) {
        return getBounds().overlaps(
            new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight())
        );
    }

    private void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        Rectangle dummyRect = getBounds();
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                PlayerActor player = (PlayerActor) actor;
                Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
                if (dummyRect.overlaps(playerRect)) {
                    System.out.println("Dummy collided with the player!");
                    // Possibly do damage to player or dummy, etc.
                }
            }
        }
    }

    public void takeDamage(int amount) {
        // Delegate to stats
        stats.takeDamage(amount);
        System.out.println("Dummy took " + amount + " damage! Health now " + stats.getHealth());

        if (stats.isDead()) {
            remove();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
