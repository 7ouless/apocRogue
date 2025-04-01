// BaseDamageableActor.java
package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actors.superClasses.DamageableActor;
import io.github.apocRogue.globals.physics.PhysicalActor;
import com.badlogic.gdx.math.Rectangle;

public abstract class BaseDamageableActor extends PhysicalActor implements DamageableActor {
    protected int health;
    protected int maxHealth;

    public BaseDamageableActor(Texture texture, int health, int maxHealth) {
        super(texture);  // OK now, because PhysicalActor extends Image
        this.health = health;
        this.maxHealth = maxHealth;
    }

    @Override
    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) remove();
    }

    @Override
    public int getHealth() {
        return health;
    }

}
