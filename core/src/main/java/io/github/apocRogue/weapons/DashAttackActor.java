package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import io.github.apocRogue.actors.playerEntity.PlayerActor;


public class DashAttackActor extends BaseAttackActor {

    private final PlayerActor player;
    private final boolean     facingRight;
    private final float       dashVel;
    private float             timer;

    private static final float SCALE = 0.3f;

    public DashAttackActor(Texture tex,
                           PlayerActor player,
                           int damage,
                           Stage stage,
                           float rawSpeed,
                           float rawDuration) {
        super(tex, damage, stage);

        this.player      = player;
        this.facingRight = player.isFacingRight();

        float speed    = rawSpeed    > 0 ? rawSpeed    : player.dashSpeed;
        float duration = rawDuration > 0 ? rawDuration : player.dashDuration;

        dashVel = speed * (facingRight ? 1f : -1f);
        timer   = duration;

        setOrigin(Align.center);
        setScaleX(SCALE * (facingRight ? 1f : -1f));
        setScaleY(SCALE);
        layout();

        player.setWeaponDashing(true);
    }

    @Override
    public void act(float delta) {
        super.act(delta);


        timer -= delta;
        if (timer <= 0f) {
            player.velocityX = 0f;
            player.setWeaponDashing(false);
            remove();
            return;
        }

        //Broken, doesnt go to player
        float dx = dashVel * delta;
        player.moveBy(dx, 0f);
        player.velocityX = dashVel;

        float w = getWidth()  * Math.abs(getScaleX());
        float h = getHeight() * Math.abs(getScaleY());
        float offX = facingRight ? player.getWidth() : -w;
        float offY = (player.getHeight() - h) * .5f;
        setPosition(player.getX() + offX, player.getY() + offY);
    }
}
