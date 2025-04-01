package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actorAi.FlyingAi;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class FlyingEnemyActor extends EnemyActor {

    public FlyingEnemyActor(Texture texture, float x, float y) {
        super(texture, x, y, new StatsComponent(50, 50, 5, 0, 200, 0, 1, 1000));
        // Assign the flying AI behavior.
        this.aiBehavior = new FlyingAi();
        // Remove any call to setAffectedByGravity if it doesn't exist.
    }

    @Override
    public void act(float delta) {
        // No call to super.act(delta) if we want to skip certain parent's code
        // But if you want collisions from GravitySystem, do:

        // Gravity is 0 => no downward pull
        GravitySystem.applyGravityAndPhysics(this, delta, 0f);

        // Then your AI
        if (aiBehavior != null) {
            aiBehavior.updateAI(this, delta);
        }
    }

}
