package io.github.apocRogue.globals.physics;

import io.github.apocRogue.globals.physics.PhysicalActor;

public class KnockbackProcessor {

    private static final float DECAY_RATE = 10f;


    public static void applyKnockback(PhysicalActor actor, float forceX, float forceY) {
        actor.velocityX = forceX;
        actor.velocityY = forceY;
        actor.isOnGround = false;
    }

    public static void updateKnockback(PhysicalActor actor, float delta) {
        actor.velocityX = approachZero(actor.velocityX, DECAY_RATE * delta);
        // vertical velocity will be taken care of by your gravity logic elsewhere
    }

    private static float approachZero(float v, float amount) {
        if (v > amount)  return v - amount;
        if (v < -amount) return v + amount;
        return 0f;
    }
}
