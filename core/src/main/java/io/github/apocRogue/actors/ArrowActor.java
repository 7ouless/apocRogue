package io.github.apocRogue.actors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.weapons.DamageNumber;

public class ArrowActor extends Image {
    private Vector2 velocity;        // Arrow's current velocity vector (pixels per second)
    private float gravity = -300f;   // Gravity acceleration (pixels per second squared)
    private Stage stage;
    private int damage = 10;

    public ArrowActor(Texture arrowTexture, float startX, float startY, float targetX, float targetY, Stage stage) {
        super(arrowTexture);
        setPosition(startX, startY);
        this.stage = stage;

        // Compute direction vector from the start to target position, then normalize it.
        Vector2 directionVector = new Vector2(targetX - startX, targetY - startY).nor();

        // Set an initial speed. Adjust this value to your liking.
        float initialSpeed = 1000f;
        velocity = new Vector2(directionVector).scl(initialSpeed);

        // Rotate the arrow to face the initial direction.
        setRotation(velocity.angleDeg());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Apply gravity to the y-component of velocity.
        velocity.y += gravity * delta;

        // Update the arrow's position using the velocity vector.
        setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);

        // Update the rotation to match the new velocity direction (optional)
        setRotation(velocity.angleDeg());

        // Check for collisions with DummyActors.
        checkCollisionWithDummies();

        // Check for collisions with TileActors (floors/platforms).
        checkCollisionWithTile();

    }

    private void checkCollisionWithDummies() {
        Rectangle arrowRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            if (actor instanceof DummyActor) {
                DummyActor dummy = (DummyActor) actor;
                if (arrowRect.overlaps(dummy.getBounds())) {
                    dummy.takeDamage(damage);
                    DamageNumber dmgNum = new DamageNumber(String.valueOf(damage),
                        new com.badlogic.gdx.scenes.scene2d.ui.Skin(
                            com.badlogic.gdx.Gdx.files.internal("ui/uiskin.json")),
                        dummy.getX() + dummy.getWidth() / 2f,
                        dummy.getY() + dummy.getHeight());
                    stage.addActor(dmgNum);
                    remove();
                    break;
                }
            }
        }
    }

    private void checkCollisionWithTile() {
        if (getStage() == null) return;
        Rectangle arrowRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (arrowRect.overlaps(tile.getBounds())) {
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        // On collision with a floor or platform, remove the arrow.
                        remove();
                        break;
                    }
                }
            }
        }
    }
}
