package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actorAi.CompositeFlyingAIBehavior;
import io.github.apocRogue.actorAi.FlyingAi;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class FlyingEnemyActor extends EnemyActor {

    public FlyingEnemyActor(Texture texture, float x, float y) {
        super(texture, x, y, new StatsComponent(50, 50, 5, 0, 200, 0, 1, 1000, 5));
        // Assign the flying AI behavior.
        this.aiBehavior = new CompositeFlyingAIBehavior();
        // Remove any call to setAffectedByGravity if it doesn't exist.
    }

    @Override
    public void act(float delta) {
        // No call to super.act(delta) if we want to skip certain parent's code
        // But if you want collisions from GravitySystem, do:
        super.act(delta);

        // Gravity is 0 => no downward pull
        GravitySystem.applyGravityAndPhysics(this, delta, getGravityFactor());

        // Then your AI
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }
    }

    @Override
    protected float getGravityFactor() {
        return 0f;
    }
}
