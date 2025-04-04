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
    private int projectileValue;
    public ArrowActor(Texture texture,
                      float startX, float startY,
                      float targetX, float targetY,
                      Stage stage,
                      int damage,
                      int projectileValue) {
        super(texture, damage, stage);
        setPosition(startX, startY);

        // Compute direction from start to target
        Vector2 dir = new Vector2(targetX - startX, targetY - startY).nor();
        projectileValue = projectileValue * 1000;
        velocity = dir.scl(projectileValue);

        // Rotate the arrow to face that direction
        setRotation(velocity.angleDeg());
    }
}
