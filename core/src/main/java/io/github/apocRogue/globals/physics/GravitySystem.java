package io.github.apocRogue.globals.physics;

public class GravitySystem {

    private static final float BASE_GRAVITY = -600f;

    public static void applyGravityAndPhysics(PhysicalActor actor, float delta, float gravityFactor) {
        // Reset ground state.
        actor.isOnGround = false;

        // Apply gravity.
        float gravityToApply = BASE_GRAVITY * gravityFactor;
        actor.velocityY += gravityToApply * delta;

        // Store old positions.
        float oldX = actor.getX();
        float oldY = actor.getY();

        // Apply vertical movement.
        actor.setY(actor.getY() + actor.velocityY * delta);

        // Resolve collisions using our dedicated collision class.
        TileCollisionHandler.resolveCollisions(actor, oldX, oldY);
    }
}
