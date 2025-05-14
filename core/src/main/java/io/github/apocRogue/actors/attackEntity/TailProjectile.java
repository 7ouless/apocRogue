package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.weapons.RangedAttackActor;
import io.github.apocRogue.weapons.Weapon;

public class TailProjectile extends RangedAttackActor {
    private static final Texture TEXTURE = new Texture("ui/scorpion-tail.png");
    private static final int     DAMAGE  = 12;
    private static final int     SPEED   = -500;    // forward speed
    private static final float   ARC_DEG = -20f;
    private static final float SCALE = 0.08f;

    // a “silent” weapon so RangedAttackActor never tries to play sounds
    private static final Weapon  SILENT_WEAPON = new Weapon(
        "radiated_tail",
        "Radiated Wolf Tail",
        DAMAGE,
        TEXTURE,
        true,
        0,
        "",
        0,
        0,
        0f,
        0f,
        0f

    );



    public TailProjectile(WolfActor owner, float tx, float ty) {
        super(
            owner.findPlayer(),
            TEXTURE,
            DAMAGE,
            owner.getStage(),
            SILENT_WEAPON,
            new Vector2()
        );

        setSize(
            TEXTURE.getWidth() * SCALE,
            TEXTURE.getHeight() * SCALE
        );
        setOrigin(
            getWidth()  * 0.1f,
            getHeight() * 0.1f
        );

        // 1) Spawn high above the wolf’s head
        float centerX = owner.getX() + owner.getWidth()/2f  - getWidth()/2f;
        float spawnY  = owner.getY() + owner.getHeight()
            + 20f;
        setPosition(centerX, spawnY);

        this.gravity = -300f;

        boolean fr = owner.isFacingRight();
        float angleDeg = fr
            ? ARC_DEG
            : 180f - ARC_DEG;
        // build the unit vector from that angle
        Vector2 dir = new Vector2(
            MathUtils.cosDeg(angleDeg),
            MathUtils.sinDeg(angleDeg)
        );
        // set velocity and visual rotation
        this.velocity = dir.scl(SPEED);
        setRotation(angleDeg);

        // (optional) log it
        Gdx.app.log("TailProjectile",
            "spawned at "+centerX+","+spawnY+" dir="+dir);
    }

    @Override
    public void act(float delta) {
        // 1) gravity + move
        velocity.y += gravity * delta;
        setPosition(getX() + velocity.x * delta,
            getY() + velocity.y * delta);

        // 2) rotate to face travel dir
        if (velocity.len2() > 0) setRotation(velocity.angleDeg());

        // 3) tile collision => disappear
        if (getStage() != null) {
            Rectangle proj = new Rectangle(getX(), getY(), getWidth(), getHeight());
            for (Actor a : new Array<>(getStage().getActors())) {
                if (a instanceof TileActor) {
                    if (proj.overlaps(((TileActor)a).getBounds())) {
                        remove();
                        return;
                    }
                }
            }
        }

        // 4) player collision => damage & disappear
        if (getStage() != null) {
            Rectangle proj = new Rectangle(getX(), getY(), getWidth(), getHeight());
            for (Actor a : new Array<>(getStage().getActors())) {
                if (a instanceof PlayerActor) {
                    PlayerActor p = (PlayerActor)a;
                    if (proj.overlaps(p.getBounds())) {
                        p.takeDamage(DAMAGE);
                        remove();
                        return;
                    }
                }
            }
        }
    }
}
