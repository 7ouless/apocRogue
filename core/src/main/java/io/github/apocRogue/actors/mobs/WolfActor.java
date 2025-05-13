// WolfActor.java
package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actorAi.wolfAI.WolfStateMachine;
import io.github.apocRogue.actors.attackEntity.TailProjectile;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.stats.StatsComponent;

public class WolfActor extends EnemyActor {
    private static final float SCALE = 0.09f;
    private final Texture normalTexture;
    private final Texture attackTexture;
    private Texture activeTexture;
    private boolean facingRight = true;

    private boolean radiated = false;
    private float shootCooldown = 0f;

    // track last frame's X to compute actual movement
    private float previousX;

    public WolfActor(Texture normalTexture,
                     Texture attackTexture, float x, float y) {
        super(normalTexture, x, y,
            new StatsComponent(50, 50, 5, 0, 275, 0, 1, 300, 8)
        );
        this.normalTexture = normalTexture;
        this.attackTexture = attackTexture;
        this.activeTexture = normalTexture;

        setSize(normalTexture.getWidth()  * SCALE,
            normalTexture.getHeight() * SCALE);
        setOrigin(getWidth()/2f, getHeight()/2f);
        this.previousX = x;
        setAIBehavior(new WolfStateMachine());
    }

    public PlayerActor findPlayer() {
        if (getStage() == null) return null;
        for (Actor a : getStage().getActors()) {
            if (a instanceof PlayerActor) return (PlayerActor)a;
        }
        return null;
    }

    public boolean canSeePlayer(PlayerActor p) {
        if (p == null) return false;
        float baseRange = getStats().sightSens();
        float range = radiated
            ? baseRange * 2f
            : baseRange;
        return lineOfSight.canSeeTarget(this, p, range, getStage());
    }

    public float distanceToPlayer(PlayerActor p) {
        if (p == null) return Float.MAX_VALUE;
        return (float)Math.hypot(p.getX() - getX(), p.getY() - getY());
    }

    public void setAttackMode(boolean attacking) {
        activeTexture = attacking ? attackTexture : normalTexture;
    }


    public boolean isRadiated() {
        return radiated;
    }

    public void setRadiated(boolean radiated) {
        this.radiated = radiated;
    }


    public void updateRadiationTimer(float delta) {
        if (shootCooldown > 0) shootCooldown -= delta;
    }

    public void resetShootCooldown() {
        this.shootCooldown = 0f;
    }


    public void shootTailSting(float tx, float ty) {
        if (shootCooldown > 0) return;
        shootCooldown = 3f;
        TailProjectile proj = new TailProjectile(this, tx, ty);
        getStage().addActor(proj);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // compute how far we actually moved
        float dx = getX() - previousX;
        if (dx < 0)      facingRight = true;
        else if (dx > 0) facingRight = false;
        previousX = getX();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw around our origin, flipping via scaleX = ±1
        batch.draw(
            activeTexture,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            facingRight ? 1f : -1f, 1f,
            getRotation(),
            0, 0,
            activeTexture.getWidth(), activeTexture.getHeight(),
            false, false
        );
    }
}
