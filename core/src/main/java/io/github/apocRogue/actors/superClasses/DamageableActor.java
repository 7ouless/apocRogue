package io.github.apocRogue.actors.superClasses;

import com.badlogic.gdx.math.Rectangle;


public interface DamageableActor {

    Rectangle getBounds();

    void takeDamage(int amount);

    int getHealth();
}
