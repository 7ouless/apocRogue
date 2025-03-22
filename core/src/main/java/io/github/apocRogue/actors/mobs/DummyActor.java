package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.apocRogue.actorAi.chaseAi;
import io.github.apocRogue.actorAi.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.stats.StatsComponent;  // <--- import your StatsComponent
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

public class DummyActor extends EnemyActor {

    // Remove "private int health = 50;"
    // Instead, store a StatsComponent
    //private StatsComponent stats;

    // Movement & physics
    private float gravity = -600f;
    private boolean isOnGround = false;
    // Jump logic

    // We can also store or retrieve speed from stats if we want
    private float maxSpeed; // read from stats?

    public DummyActor(Texture texture, float x, float y) {
        // Pass in your stats to the super constructor
        super(
            texture,
            x,
            y,
            new StatsComponent(50, 50, 5, 0, 60, 0, 1, 1000) // example stats
        );
        setAIBehavior(new chaseAi());


    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }


}
