package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actorAi.baseAI.SoundAlertComponent;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.physics.KnockbackProcessor;
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

    public PlayerActor findPlayer() {
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
                if (getBounds().overlaps(player.getBounds())) {
                    int damage = stats.getStrength();
                    // 1) apply damage
                    player.takeDamage(damage);
                    // 2) apply knockback impulse
                    float dir   = player.getX() < getX() ? -1f : 1f;
                    float forceX = dir * 5000f;
                    float forceY = 800f;
                    KnockbackProcessor.applyKnockback(player, forceX, forceY);
                    player.startKnockback();
                }
            }
        }
    }
}

