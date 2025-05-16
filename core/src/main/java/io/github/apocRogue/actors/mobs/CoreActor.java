package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actorAi.coreAi.CoreStateMachine;
import io.github.apocRogue.actors.attackEntity.CoreProjectile;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.stats.StatsComponent;

public class CoreActor extends EnemyActor {
    private static final float SCALE_WIDTH               = 0.22f;
    private static final float SCALE_HEIGHT              = 0.22f;

    private final Texture normalTexture, attackTexture;
    private Texture activeTexture;

    private boolean facingRight = true;
    private float previousX;
    private float shootCooldown = 0f;

    public CoreActor(Texture normalTexture,
                     Texture attackTexture,
                     float x, float y) {
        super(normalTexture, x, y,
            new StatsComponent(200, 50, 20, 0, 50, 0, 1, 300, 8)
        );
        this.normalTexture = normalTexture;
        this.attackTexture = attackTexture;
        this.activeTexture = normalTexture;

        setSize(normalTexture.getWidth() * SCALE_WIDTH,
            normalTexture.getHeight() * SCALE_HEIGHT);
        setOrigin(getWidth()/2f, getHeight()/2f);


        this.previousX = x;
        setAIBehavior(new CoreStateMachine());
    }


    public boolean isFacingRight() {
        return facingRight;
    }

    public PlayerActor findPlayer() {
        if (getStage() == null) return null;
        for (Actor a : getStage().getActors())
            if (a instanceof PlayerActor) return (PlayerActor)a;
        return null;
    }

    public void resetShootCooldown() {
        shootCooldown = 0f;
    }

    public boolean canSeePlayer(PlayerActor p) {
        if (p == null) return false;
        return lineOfSight.canSeeTarget(
            this, p,
            getStats().sightSens() * 2f,
            getStage()
        );
    }

    public float distanceToPlayer(PlayerActor p) {
        if (p == null) return Float.MAX_VALUE;
        return (float)Math.hypot(p.getX() - getX(), p.getY() - getY());
    }

    public void setAttackMode(boolean attacking) {
        activeTexture = attacking ? attackTexture : normalTexture;

    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // flip logic
        float dx = getX() - previousX;
        if (dx < 0)      facingRight = true;
        else if (dx > 0) facingRight = false;
        previousX = getX();

        if (shootCooldown > 0f) shootCooldown -= delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
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

    public void shootCoreSting(float tx, float ty) {
        if (shootCooldown > 0f) return;
        shootCooldown = 3f;
        CoreProjectile proj = new CoreProjectile(this, tx, ty);
        getStage().addActor(proj);
    }
}
