package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.physics.SoundPhysics;
import io.github.apocRogue.weapons.RangedAttackActor;
import io.github.apocRogue.weapons.Weapon;

/**
 * A concrete arrow that uses the RangedAttackActor base logic.
 * We set velocity in the constructor based on a target location.
 */
public class ArrowActor extends RangedAttackActor {
    // You can now use the inherited noiseLevel field from RangedAttackActor.
    public ArrowActor(Texture texture,
                      float startX, float startY,
                      float targetX, float targetY,
                      Stage stage,
                      int damage,
                      int projectileValue,
                      int noiseLevel, Weapon weapon, PlayerActor player, Vector2 direction ) {
        super(player, texture, damage, stage, weapon, direction);
        SoundPhysics.emitSound(
            new Vector2(getX(), getY()),
            weapon.getFlightNoiseIntensity(),
            weapon.getFlightNoiseRadius(),
            SoundPhysics.SoundType.PROJECTILE_FLIGHT,
            player.getStage()
        );
        // Set the noise level from the weapon.
        this.noiseLevel = noiseLevel;
        setPosition(startX, startY);

        // Compute direction from start to target
        Vector2 dir = new Vector2(targetX - startX, targetY - startY).nor();
        projectileValue = projectileValue * 1000;
        velocity = dir.scl(projectileValue);

        // Rotate the arrow to face that direction
        setRotation(velocity.angleDeg());
    }
}
