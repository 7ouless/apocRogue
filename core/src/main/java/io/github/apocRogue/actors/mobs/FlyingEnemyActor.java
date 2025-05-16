package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.apocRogue.actorAi.flierAI.CompositeFlyingAIBehavior;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class FlyingEnemyActor extends EnemyActor {
    private static final float SCALE = 0.09f;
    private static final int BASE_SPEED = 80;

    private final Texture texture;
    private final boolean radiated;
    private boolean facingRight = true;
    private float previousX;


    public FlyingEnemyActor(Texture texture, float x, float y, boolean radiated) {

        super(
            texture,
            x, y,
           new StatsComponent(
                50,
                50,
                5,
                0,
                BASE_SPEED * (radiated ? 2 : 1),
                0,
                1,
                1000,
                5
            )
        );

        this.texture    = texture;
        this.radiated   = radiated;
        this.aiBehavior = new CompositeFlyingAIBehavior();

        float w = texture.getWidth()  * SCALE;
        float h = texture.getHeight() * SCALE;
        setSize(w, h);
        setOrigin(w * .5f, h * .5f);

        this.previousX = getX();
    }

    public boolean isRadiated() {
        return radiated;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        float dx = getX() - previousX;
        facingRight = dx > 0;
        previousX   = getX();

        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());
        if (aiBehavior != null) aiBehavior.updateAI(this, delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
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
