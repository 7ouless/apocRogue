package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.math.Rectangle;

/**
 * Any actor that can be damaged by attacks or projectiles should implement this interface.
 */
public interface DamageableActor {
    /**
     * Returns the bounding box of this actor (used in collision checks).
     */
    Rectangle getBounds();

    /**
     * Applies damage to this actor. Implementations can decide how to handle health,
     * death conditions, or playing a hit animation.
     *
     * @param amount How much damage to take
     */
    void takeDamage(int amount);

    /**
     * Returns the current health of this actor (if relevant for your UI or logic).
     */
    int getHealth();
}
