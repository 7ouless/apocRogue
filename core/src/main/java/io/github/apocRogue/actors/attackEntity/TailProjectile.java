package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.weapons.RangedAttackActor;

public class TailProjectile extends RangedAttackActor {
    private static final Texture TEXTURE = new Texture("ui/scorpion-tail.png");
    private static final int     DAMAGE  = 12;
    private static final int     SPEED   = 200;      // slower than before
    private static final float   SCALE   = 0.2f;     // shrink sprite

    private final WolfActor owner;

    public TailProjectile(WolfActor owner, float tx, float ty) {
        super(
            owner.findPlayer(),
            TEXTURE,
            DAMAGE,
            owner.getStage(),
            null,
             new Vector2(tx - owner.getX(), ty - owner.getY()).nor()
        );

        this.owner = owner;

        // shrink the sprite
        setSize(TEXTURE.getWidth() * SCALE,
            TEXTURE.getHeight() * SCALE);
        setOrigin(getWidth()/2f, getHeight()/2f);

        // position & velocity
        Vector2 dir = new Vector2(tx - owner.getX(), ty - owner.getY()).nor();
        velocity = dir.scl(SPEED);
        setRotation(velocity.angleDeg());

        float spawnOffset = owner.getWidth() * 0.6f;   // 60% of the wolf’s width
        setPosition(
            owner.getX() + dir.x * spawnOffset,
            owner.getY() + dir.y * spawnOffset
        );

        // soften gravity so it arcs more
        this.gravity = -100f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);    // runs BaseAttackActor collision + gravity + map-tile checks
        checkCollisionWithPlayer();
    }

    private void checkCollisionWithPlayer() {
        if (getStage() == null) return;
        Rectangle projBounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor a : new Array<>(getStage().getActors())) {
            if (a instanceof PlayerActor) {
                PlayerActor p = (PlayerActor)a;
                // only hit the player, never the wolf owner
                if (projBounds.overlaps(p.getBounds())) {
                    p.takeDamage(DAMAGE);
                    remove();
                    break;
                }
            }
        }
    }
}
