package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.weapons.RangedAttackActor;

/**
 * A concrete arrow that uses the RangedAttackActor base logic.
 * We set velocity in the constructor based on a target location.
 */
public class ArrowActor extends RangedAttackActor {

    public ArrowActor(Texture texture,
                      float startX, float startY,
                      float targetX, float targetY,
                      Stage stage,
                      int damage) {
        super(texture, damage, stage);
        setPosition(startX, startY);

        // Compute direction from start to target
        Vector2 dir = new Vector2(targetX - startX, targetY - startY).nor();
        float initialSpeed = 1000f;
        velocity = dir.scl(initialSpeed);

        // Rotate the arrow to face that direction
        setRotation(velocity.angleDeg());
    }
}
