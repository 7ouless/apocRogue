package io.github.apocRogue.globals.physics;

public class GravitySystem {

    private static final float BASE_GRAVITY = -600f;

    public static void applyGravityAndPhysics(PhysicalActor actor, float delta, float gravityFactor) {
        //reset ground state
        actor.isOnGround = false;

        //applyin gravity
        float gravityToApply = BASE_GRAVITY * gravityFactor;
        actor.velocityY += gravityToApply * delta;

        //last frame positions
        float oldX = actor.getX();
        float oldY = actor.getY();

        //vertical movement
        actor.setY(actor.getY() + actor.velocityY * delta);

        //collision resolving
        TileCollisionHandler.resolveCollisions(actor, oldX, oldY);
    }
}
