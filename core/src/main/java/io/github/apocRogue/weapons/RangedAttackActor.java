package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

/**
 * A base class for ranged attacks (projectiles), e.g. arrows, fireballs.
 * It has velocity, gravity, and breaks on tile collision by default.
 */
public abstract class RangedAttackActor extends BaseAttackActor {
    protected Vector2 velocity = new Vector2(0, 0);
    protected float gravity = -300f; // downward acceleration
    protected int noiseLevel; // Added field to store the noise level


    public RangedAttackActor(Texture texture, int damage, Stage stage) {
        super(texture, damage, stage);
    }
    @Override
    public void act(float delta) {
        // 1) Perform the default collision check in BaseAttackActor
        super.act(delta);

        // 2) Apply gravity
        velocity.y += gravity * delta;

        // 3) Move according to velocity
        setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);

        // 4) Optionally rotate to face movement direction
        if (velocity.len2() > 0) {
            setRotation(velocity.angleDeg());
        }

        // 5) Collide with the map
        checkCollisionWithTile();
    }

    protected void checkCollisionWithTile() {
        if (stage == null) return;
        Rectangle projectileRect = new Rectangle(getX(), getY(), getWidth(), getHeight());

        for (Actor actor : stage.getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (projectileRect.overlaps(tile.getBounds())) {
                    // Get collision position
                    Vector2 collisionPoint = new Vector2(getX(), getY());
                    // Use the noiseLevel stored in this projectile.
                    float noise = noiseLevel; // or modify this value as needed
                    // Optionally, define a hearing range.
                    float hearingRange = 500f;
                    for (Actor a : stage.getActors()) {
                        if (a instanceof EnemyActor) {
                            EnemyActor enemy = (EnemyActor) a;
                            if (collisionPoint.dst(enemy.getX(), enemy.getY()) < hearingRange) {
                                enemy.getAlertComponent().triggerAlert(collisionPoint, noise);
                            }
                        }
                    }
                    // Remove the projectile after collision.
                    remove();
                    break;
                }
            }
        }
    }


}
