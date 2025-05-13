package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;


public abstract class MeleeAttackActor extends BaseAttackActor {

    private float timeAlive = 0f;
    private float maxDuration = 0.2f; // The slash disappears after 0.2 seconds
    private boolean facingRight;
    private float moveSpeed = 200f;   // how fast it moves horizontally

    public MeleeAttackActor(Texture texture, PlayerActor player, int damage, Stage stage) {
        // We pass the texture, damage, and stage up to BaseAttackActor
        super(texture, damage, stage);
        this.facingRight = player.isFacingRight();
        float extraOffset = 80f;

        // Position slash relative to the player's location
        float offsetX = facingRight ? player.getWidth() : -getWidth() + extraOffset;
        float offsetY = (player.getHeight() / 2f) - (getHeight() / 2f);
        setPosition(player.getX() + offsetX, player.getY() + offsetY);

        // Flip horizontally if the player is facing left
        if (!facingRight) {
            setScaleX(-1);
        }
    }

    @Override
    public void act(float delta) {
        // 1) Perform the default collision check in BaseAttackActor
        super.act(delta);

        // 2) Move a little in the facing direction (optional)
        float moveDist = moveSpeed * delta;
        if (facingRight) {
            setX(getX() + moveDist);
        } else {
            setX(getX() - moveDist);
        }

        // 3) Track lifetime and remove if time is up
        timeAlive += delta;
        if (timeAlive >= maxDuration) {
            remove();
        }
    }
}
