package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actorAi.flierAI.CompositeAIBehavior;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;  // <--- import your StatsComponent


public class DummyActor extends EnemyActor {

    // Remove "private int health = 50;"
    // Instead, store a StatsComponent
    //private StatsComponent stats
    // We can also store or retrieve speed from stats if we want
    private float maxSpeed; // read from stats?

    public DummyActor(Texture texture, float x, float y) {
        // Pass in your stats to the super constructor
        super(
            texture,
            x,
            y,
            new StatsComponent(50, 50, 5, 0, 400, 0, 1, 300, 8) // example stats
        );
        setAIBehavior(new CompositeAIBehavior());


    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);

    }


}
