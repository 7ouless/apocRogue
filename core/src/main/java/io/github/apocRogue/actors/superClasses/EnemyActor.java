package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.apocRogue.actorAi.AIBehavior;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.GravitySystem;
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

    // If you want them accessible to GravitySystem, keep them public or use getters
    public float velocityX = 0f;
    public float velocityY = 0f;
    public boolean isOnGround = false;

    // Gravity used to be here, but we handle it in GravitySystem now
    // protected float gravity = -600f;

    protected float jumpCooldown = 2f;
    protected float jumpTimer = 0f;
    protected float jumpPower = 600f;
    protected AIBehavior aiBehavior;
    protected StatsComponent stats;

    public EnemyActor(Texture texture, float x, float y, StatsComponent stats) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
        this.stats = stats;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Instead of applying gravity/collisions here, call GravitySystem:
        float gravityFactor = 1f; // Could be 0f for flying, 2f for heavy area, etc.
        GravitySystem.applyGravityAndPhysics(this, delta, gravityFactor);

        // AI update
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }

        // e.g., check collision with player
        checkCollisionWithPlayer();
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
