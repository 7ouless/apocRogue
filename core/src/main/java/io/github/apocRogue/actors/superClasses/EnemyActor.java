package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import io.github.apocRogue.actorAi.AIBehavior;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.stats.StatsComponent;

/**
 * A base class for any AI-driven enemy/mob in your game.
 * It holds common fields like stats, velocity, collision, etc.
 */
public class EnemyActor extends PhysicalActor implements DamageableActor {
    // Existing fields...
    protected StatsComponent stats;

    // Fields for the alert system
    private Vector2 alertPosition = new Vector2();
    private float alertness = 0f;
    private boolean alerted = false;

    // Public fields for physics handling.
    public float velocityX = 0f;
    public float velocityY = 0f;
    public boolean isOnGround = false;

    protected float jumpCooldown = 2f;
    protected float jumpTimer = 0f;
    protected float jumpPower = 600f;
    protected AIBehavior aiBehavior;
    public float health = 0f;
    public EnemyActor(Texture texture, float x, float y, StatsComponent stats) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
        this.stats = stats;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply gravity and physics.
        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());

        // AI update
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }

        // e.g., check collision with player.
        checkCollisionWithPlayer();
    }

    protected float getGravityFactor() {
        return 1f; // default for ground-based enemies
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
                    // Collision handling (e.g., damage player) can be implemented here.
                }
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        if (stats.getHealth() <= 0) {
            remove();  // remove from stage, or handle death
        }
    }

    @Override
    public int getHealth() {
        return (int) this.health;
    }

    @Override
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

    // Alert system getters and setters

    public Vector2 getAlertPosition() {
        return alertPosition;
    }

    public void setAlertPosition(Vector2 alertPosition) {
        this.alertPosition = alertPosition;
    }

    public float getAwareness() {
        return stats.sightSens();
    }
    public float getAlertness() {
        return alertness;
    }

    public void setAlertness(float alertness) {
        this.alertness = alertness;
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void setAlerted(boolean alerted) {
        this.alerted = alerted;
    }


    /**
     * Method to update the enemy's alert state.
     * Increase alertness based on noise level and record the position.
     */
    public void alert(Vector2 soundPosition, float noiseLevel) {
        setAlertness(getAlertness() + noiseLevel);
        setAlertPosition(soundPosition);
        setAlerted(true);
    }
}
