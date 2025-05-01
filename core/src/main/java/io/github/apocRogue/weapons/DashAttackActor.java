package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.weapons.BaseAttackActor;

public class DashAttackActor extends BaseAttackActor {
    private final PlayerActor player;
    private final boolean    facingRight;
    private final float      offsetX, offsetY;
    private float            timer;


    public DashAttackActor(Texture texture,
                           PlayerActor player,
                           int damage,
                           Stage stage,
                           float rawSpeed,
                           float rawDuration) {
        super(texture, damage, stage);
        this.player      = player;
        // bump both by 20%
        float speed     = rawSpeed    * 1.2f;
        this.timer      = rawDuration * 1.2f;
        this.facingRight = player.isFacingRight();

        // center origin so scale/flip are stable
        setOrigin(Align.center);

        // half-size sprite, flip horizontally if needed
        float baseScale = 0.5f;
        setScaleX(baseScale * (facingRight ?  1f : -1f));
        setScaleY(baseScale);

        // compute offset using the *scaled* dimensions
        float scaledW = getWidth()  * Math.abs(getScaleX());
        float scaledH = getHeight() * Math.abs(getScaleY());
        offsetX = facingRight
            ? player.getWidth()        // flush-right
            : -scaledW;                 // flush-left
        offsetY = (player.getHeight() - scaledH) * 0.5f;

        // initial placement
        setPosition(player.getX() + offsetX,
            player.getY() + offsetY);

        // lock controls & launch
        player.setWeaponDashing(true);
        player.velocityX = speed * (facingRight ? 1 : -1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);  // collision → damage

        timer -= delta;
        if (timer <= 0f) {
            // end dash
            player.velocityX      = 0f;
            player.setWeaponDashing(false);
            remove();
        } else {
            // keep it glued right at the edge
            setPosition(player.getX() + offsetX,
                player.getY() + offsetY);
        }
    }
}
