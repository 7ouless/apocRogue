package io.github.apocRogue.actors;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.weapons.DamageNumber;

public class ArrowActor extends Image {
    private float speed = 300f; // pixels per second
    // Optionally track direction if your player can face left or right
    private float direction = 1f; // +1 for right, -1 for left
    private Stage stage;
    private int damage = 10;
    public ArrowActor(Texture arrowTexture, float x, float y, float direction, Stage stage) {
        super(arrowTexture);
        setPosition(x, y);
        this.stage = stage;
        this.direction = direction;
        // If you have an arrow image, rotate or flip if needed
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Store the current position before updating
        float oldX = getX();

        // Update the arrow's position
        setX(getX() + speed * direction * delta);

        // Check for collisions with dummies
        checkCollisionWithDummies();

        // Pass the old position to the tile collision check
        checkCollisionWithTile(oldX);
    }

    private void checkCollisionWithDummies() {
        Rectangle arrowRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            if (actor instanceof DummyActor) {
                DummyActor dummy = (DummyActor) actor;
                // If slashRect overlaps the dummy
                if (arrowRect.overlaps(dummy.getBounds())) {
                    // Deal damage
                    dummy.takeDamage(damage);

                    // Spawn damage number
                    // Suppose you have a static or accessible 'skin' for labels
                    // or pass it in the constructor.
                    // For quick example:
                    DamageNumber dmgNum = new DamageNumber(String.valueOf(damage),
                        new com.badlogic.gdx.scenes.scene2d.ui.Skin(
                            com.badlogic.gdx.Gdx.files.internal("ui/uiskin.json")),
                        dummy.getX() + dummy.getWidth()/2f,
                        dummy.getY() + dummy.getHeight()
                    );
                    stage.addActor(dmgNum);

                    // If you want the slash to disappear after hitting once
                    remove();
                    break; // exit loop so we don't hit multiple dummies at once
                }
            }
        }
    }

    private void checkCollisionWithTile(float oldX) {
        if (getStage() == null) return;
        Rectangle arrowRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            // Correct type check: look for actors that are TileActor (and its subclasses)
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (arrowRect.overlaps(tile.getBounds())) {
                    // Handle collisions only for floor/platform tiles
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileLeft = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float currentLeft = getX();
                        float currentRight = getX() + getWidth();

                        // If moving left (oldX > currentX) and crossing tile's right edge:
                        if (oldX > getX() && oldX >= tileRight && currentLeft < tileRight) {
                            remove();
                        }
                        // If moving right (oldX < currentX) and crossing tile's left edge:
                        else if (oldX < getX() && oldX + getWidth() <= tileLeft && currentRight > tileLeft) {
                            remove();
                        }
                    }
                }
            }
        }


}


}
