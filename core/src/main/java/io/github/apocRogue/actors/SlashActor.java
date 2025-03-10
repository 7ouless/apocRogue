package io.github.apocRogue.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.weapons.DamageNumber;

public class SlashActor extends Image {
    private float timeAlive = 0f;
    private float maxDuration = 0.2f;
    private int damage = 10; // how much damage slash does
    private Stage stage; // reference to the stage for collision + spawning damage numbers
    private float direction = 1f; // +1 for right, -1 for left

    public SlashActor(Texture slashTexture, float x, float y, float direction, Stage stage) {
        super(slashTexture);
        setPosition(x, y);
        this.stage = stage;
        this.direction = direction;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeAlive += delta;

        // Collision check with any DummyActor
        checkCollisionWithDummies();

        // Remove slash after time is up
        if (timeAlive >= maxDuration) {
            remove();
        }
    }

    private void checkCollisionWithDummies() {
        // The slash bounding box
        Rectangle slashRect = new Rectangle(getX(), getY(), getWidth(), getHeight());

        // Loop through all actors in the stage
        for (Actor actor : stage.getActors()) {
            if (actor instanceof DummyActor) {
                DummyActor dummy = (DummyActor) actor;
                // If slashRect overlaps the dummy
                if (slashRect.overlaps(dummy.getBounds())) {
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

                    break; // exit loop so we don't hit multiple dummies at once
                }
            }
        }
    }
    private void checkCollisionWithChests() {
        Rectangle slashRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            if (actor instanceof ChestActor) {
                ChestActor chest = (ChestActor) actor;
                if (!chest.isOpened() && slashRect.overlaps(chest.getBounds())) {
                    chest.takeDamage(damage);
                    // Optionally spawn some slash effect or damage number
                }
            }
        }
    }

}
