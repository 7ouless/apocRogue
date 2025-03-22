package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.mobs.DummyActor;
import io.github.apocRogue.weapons.DamageNumber;

public class SlashActor extends Image {
    private float timeAlive = 0f;
    private float maxDuration = 0.2f; // Duration the slash remains visible (in seconds)
    private int damage = 10;
    private Stage stage;
    private boolean facingRight; // Derived from the player

    /**
     * Creates a SlashActor that appears in front of the player based on the player's facing direction.
     * @param slashTexture the texture for the slash effect
     * @param player the PlayerActor from which the slash originates
     * @param stage the stage to which the slash will be added
     */
    public SlashActor(Texture slashTexture, PlayerActor player, Stage stage) {
        super(slashTexture);
        this.stage = stage;
        this.facingRight = player.isFacingRight();

        // Position the slash relative to the player's position.
        // For example, if the player is facing right, place it slightly to the right; if left, place it to the left.
        float offsetX = facingRight ? player.getWidth() : -getWidth();
        float offsetY = player.getHeight() / 2f - getHeight() / 2f; // center vertically on the player

        setPosition(player.getX() + offsetX, player.getY() + offsetY);

        // Optionally, if your slash texture should be flipped when the player is facing left:
        if (!facingRight) {
            setScaleX(-1); // This flips the image horizontally
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeAlive += delta;

        // Optionally, you could move the slash a little in the player's facing direction.
        // For a melee slash, however, it might be static.
        // Example: move a few pixels forward:
        float moveDistance = 200f * delta; // adjust as needed
        if (facingRight) {
            setX(getX() + moveDistance);
        } else {
            setX(getX() - moveDistance);
        }

        // Check collision with targets.
        checkCollisionWithDummies();
        checkCollisionWithChests();

        // Remove the slash after its duration has elapsed.
        if (timeAlive >= maxDuration) {
            remove();
        }
    }

    private void checkCollisionWithDummies() {
        Rectangle slashRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : stage.getActors()) {
            if (actor instanceof DummyActor) {
                DummyActor dummy = (DummyActor) actor;
                if (slashRect.overlaps(dummy.getBounds())) {
                    dummy.takeDamage(damage);
                    DamageNumber dmgNum = new DamageNumber(String.valueOf(damage),
                        new com.badlogic.gdx.scenes.scene2d.ui.Skin(
                            Gdx.files.internal("ui/uiskin.json")),
                        dummy.getX() + dummy.getWidth() / 2f,
                        dummy.getY() + dummy.getHeight());
                    stage.addActor(dmgNum);
                    remove();
                    break;
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
                    // Optionally, spawn some effect or damage number here.
                }
            }
        }
    }
}
