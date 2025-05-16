package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.apocRogue.actorAi.flierAI.CompositeFlyingAIBehavior;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class FlyingEnemyActor extends EnemyActor {
    private static final float SCALE = 0.09f;
    private final Texture texture;
    private float previousX;
    private boolean facingRight = true;

    public FlyingEnemyActor(Texture texture, float x, float y) {
        super(texture, x, y, new StatsComponent(50, 50, 5, 0, 80, 0, 1, 1000, 5));
        this.texture = texture;
        this.aiBehavior = new CompositeFlyingAIBehavior();

        float w = texture.getWidth()  * SCALE;
        float h = texture.getHeight() * SCALE;
        setSize(w, h);
        setOrigin(w * 0.5f, h * 0.5f);

        this.previousX = getX();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Track movement to determine facing
        float dx = getX() - previousX;
        if (dx < 0) facingRight = false;
        else if (dx > 0) facingRight = true;
        previousX = getX();

        // Apply zero-gravity physics
        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());

        // Execute AI behavior
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Use stored texture and flip horizontally based on facing
        batch.draw(
            texture,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            facingRight ? 1f : -1f, 1f,
            getRotation(),
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    @Override
    protected float getGravityFactor() {
        return 0f;
    }
}
