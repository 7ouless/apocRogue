package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.superClasses.DamageableActor;


public abstract class BaseAttackActor extends Image {
    protected int damage;
    protected Stage stage;

    public BaseAttackActor(Texture texture, int damage, Stage stage) {
        super(texture);
        this.damage = damage;
        this.stage = stage;
    }

    @Override
    public void act(float delta) {
        // Ensure the base logic is done first. This also calls draw, etc.
        super.act(delta);

        // By default, we run a collision check each frame for damageable targets
        checkCollisionWithDamageables();
    }

    protected void checkCollisionWithDamageables() {
        if (stage == null) return;

        // Copy the actor list
        Array<Actor> copy = new Array<>(stage.getActors());

        Rectangle bounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
        for (Actor actor : copy) {
            if (actor instanceof DamageableActor) {
                DamageableActor dmgActor = (DamageableActor) actor;
                if (bounds.overlaps(dmgActor.getBounds())) {
                    dmgActor.takeDamage(damage);
                    remove();
                    break;
                }
            }
        }
    }

}
