package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;


public abstract class MeleeAttackActor extends BaseAttackActor {

    private float timeAlive = 0f;
    private float maxDuration = 0.2f;
    private boolean facingRight;
    private float moveSpeed = 200f;

    public MeleeAttackActor(Texture texture, PlayerActor player, int damage, Stage stage) {
        super(texture, damage, stage);
        this.facingRight = player.isFacingRight();
        float extraOffset = 80f;

        //to update: but does look funny
        float offsetX = facingRight ? player.getWidth() : -getWidth() + extraOffset;
        float offsetY = (player.getHeight() / 2f) - (getHeight() / 2f);
        setPosition(player.getX() + offsetX, player.getY() + offsetY);

        if (!facingRight) {
            setScaleX(-1);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);


        float moveDist = moveSpeed * delta;
        if (facingRight) {
            setX(getX() + moveDist);
        } else {
            setX(getX() - moveDist);
        }

        timeAlive += delta;
        if (timeAlive >= maxDuration) {
            remove();
        }
    }
}
