package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actorAi.flierAI.CompositeFlyingAIBehavior;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class FlyingEnemyActor extends EnemyActor {

    public FlyingEnemyActor(Texture texture, float x, float y) {
        super(texture, x, y, new StatsComponent(50, 50, 5, 0, 200, 0, 1, 1000, 5));
        this.aiBehavior = new CompositeFlyingAIBehavior();
    }

    @Override
    public void act(float delta) {

        super.act(delta);

        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());

        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }
    }

    @Override
    protected float getGravityFactor() {
        return 0f;
    }
}
