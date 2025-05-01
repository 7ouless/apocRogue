// File: io/github/apocRogue/globals/physics/MovementProcessor.java
package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class MovementProcessor {
    public static void handleHorizontalMovement(PlayerActor actor, float delta) {
        boolean movingLeft  = Gdx.input.isKeyPressed(Input.Keys.A)    || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.D)    || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        float speed = actor.getStats().getSpeed();
        if (movingLeft) {
            actor.velocityX   -= speed * delta;
            actor.facingRight  = false;
        }
        if (movingRight) {
            actor.velocityX   += speed * delta;
            actor.facingRight  = true;
        }

        // clamp horizontal speed
        if (actor.velocityX >  speed) actor.velocityX =  speed;
        if (actor.velocityX < -speed) actor.velocityX = -speed;
    }
}
