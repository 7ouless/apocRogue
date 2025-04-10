package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import io.github.apocRogue.actorAi.AIBehavior;
import io.github.apocRogue.actorAi.SoundAlertComponent;
import io.github.apocRogue.actorAi.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.stats.StatsComponent;

/**
 * A base class for any AI-driven enemy/mob in your game.
 * It holds common fields like stats, velocity, collision, etc.
 */

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.AIBehavior;
import io.github.apocRogue.actorAi.SoundAlertComponent;
import io.github.apocRogue.actorAi.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.stats.StatsComponent;

public class EnemyActor extends PhysicalActor implements DamageableActor {
    protected StatsComponent stats;
    protected AIBehavior aiBehavior;

    // The “alert” system
    private SoundAlertComponent alertComponent;

    public float velocityX = 0f;
    public float velocityY = 0f;
    public boolean isOnGround = false;
    protected float jumpCooldown = 2f;
    protected float jumpTimer = 0f;
    protected float jumpPower = 600f;

    public EnemyActor(Texture texture, float x, float y, StatsComponent stats) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
        this.stats = stats;

        // For example, set hearingThreshold = 0.2f
        this.alertComponent = new SoundAlertComponent(0.00000001f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply gravity
        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());

        // Update sound alert logic
        alertComponent.update(delta);

        // AI logic
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }

        // Possibly check collisions with Player
        checkCollisionWithPlayer();
    }

    protected float getGravityFactor() {
        return 1f;
    }

    // The “alert” calls
    public void alert(Vector2 position, float noiseLevel) {
        alertComponent.triggerAlert(position, noiseLevel);
    }

    public SoundAlertComponent getAlertComponent() {
        return alertComponent;
    }

    public boolean isAlerted() {
        return alertComponent.isAlerted();
    }

    public Vector2 getAlertPosition() {
        return alertComponent.getAlertPosition();
    }

    // ... other stuff: jump, collisions, stats, etc. ...

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        if (stats.getHealth() <= 0) {
            remove(); // or handle death
        }
    }

    @Override
    public int getHealth() {
        return stats.getHealth();
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




    /**
     * Method to update the enemy's alert state.
     * Increase alertness based on noise level and record the position.
     */

    // In EnemyActor.java
    protected float lockOnTimer = 10f;
    protected final float lockOnTimerMax = 10f;

    public void updateAlertState(float delta) {
        PlayerActor player = findPlayer();
        if (player != null) {
            boolean canSee = lineOfSight.canSeeTarget(this, player, stats.sightSens(), getStage());
            if (canSee) {
                // Calls your new setAlertPosition(...) forwarder
                alertComponent.setAlertPosition(new Vector2(player.getX(), player.getY()));
                alertComponent.setAlerted(true);
                lockOnTimer = lockOnTimerMax;
            } else {
                lockOnTimer -= delta;
                if (lockOnTimer < 0) {
                    alertComponent.setAlerted(false);
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
    protected void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                PlayerActor player = (PlayerActor) actor;
                // Assuming both EnemyActor and PlayerActor have a proper getBounds() method
                if (getBounds().overlaps(player.getBounds())) {
                    // Handle collision: for example, inflict damage or trigger an alert.
                    player.takeDamage(0);  // Adjust damage accordingly
                }
            }
        }
    }
}

