// CoreProjectile.java
package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mobs.CoreActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.weapons.RangedAttackActor;
import io.github.apocRogue.weapons.Weapon;

public class CoreProjectile extends RangedAttackActor {
    private static final Texture TEXTURE = new Texture("ui/scorpion-tail.png");
    private static final int DAMAGE = 12;
    private static final int SPEED = -350;
    private static final float ARC_DEG = -20f;
    private static final float SCALE = 0.20f;

    private static final Weapon SILENT_WEAPON = new Weapon(
        "core_tail", "Core Tail Sting", DAMAGE,
        TEXTURE, true, 0, "", 0, 0, 0f, 0f, 0f
    );

    public CoreProjectile(CoreActor owner, float tx, float ty) {
        super(owner.findPlayer(), TEXTURE, DAMAGE,
            owner.getStage(), SILENT_WEAPON, new Vector2());

        setSize(TEXTURE.getWidth() * SCALE, TEXTURE.getHeight() * SCALE);
        setOrigin(getWidth() * 0.1f, getHeight() * 0.1f);

        float cx = owner.getX() + owner.getWidth() / 2f - getWidth() / 2f;
        float sy = owner.getY() + owner.getHeight() + 20f;
        setPosition(cx, sy);

        this.gravity = -300f;

        // **use owner.isFacingRight()**
        boolean fr = owner.isFacingRight();
        float angle = fr ? ARC_DEG : 180f - ARC_DEG;
        Vector2 dir = new Vector2(
            MathUtils.cosDeg(angle),
            MathUtils.sinDeg(angle)
        );
        this.velocity = dir.scl(SPEED);
        setRotation(angle);

        Gdx.app.log("CoreProjectile", "spawned at " + cx + "," + sy + " dir=" + dir);
    }

    @Override
    public void act(float delta) {
        velocity.y += gravity * delta;
        setPosition(getX() + velocity.x * delta,
            getY() + velocity.y * delta);
        if (velocity.len2() > 0) setRotation(velocity.angleDeg());

        Rectangle proj = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor a : new Array<>(getStage().getActors())) {
            if (a instanceof TileActor && proj.overlaps(((TileActor) a).getBounds())) {
                remove();
                return;
            }
        }
        for (Actor a : new Array<>(getStage().getActors())) {
            if (a instanceof PlayerActor) {
                PlayerActor p = (PlayerActor) a;
                if (proj.overlaps(p.getBounds())) {
                    p.takeDamage(DAMAGE);
                    remove();
                    return;
                }
            }
        }
    }
}
