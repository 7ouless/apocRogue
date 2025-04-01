package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;
import com.badlogic.gdx.math.Rectangle;

public class GravitySystem {

    // The base gravity to apply in normal circumstances
    private static final float BASE_GRAVITY = -600f;

    /**
     * Applies gravity and handles tile collisions for an EnemyActor.
     *
     * @param actor The EnemyActor to update
     * @param delta The time step
     * @param gravityFactor A multiplier for gravity (e.g., 1 = normal, 0 = no gravity, 2 = double gravity)
     */
    public static void applyGravityAndPhysics(PhysicalActor actor, float delta, float gravityFactor) {
        // 1) Mark not on ground each frame
        actor.isOnGround = false;

        // 2) Apply gravity
        float gravityToApply = BASE_GRAVITY * gravityFactor;
        actor.velocityY += gravityToApply * delta;

        // 3) old positions
        float oldX = actor.getX();
        float oldY = actor.getY();

        // 4) vertical movement
        actor.setY(actor.getY() + actor.velocityY * delta);

        // 5) collisions
        handleTileCollisions(actor, oldX, oldY);
    }

    /**
     * Checks tile collisions and adjusts the actor's position accordingly (vertical & horizontal).
     *
     * @param actor The EnemyActor being updated
     * @param oldX The X position of the actor before movement
     * @param oldY The Y position of the actor before movement
     */
    private static void handleTileCollisions(PhysicalActor actor, float oldX, float oldY) {
        if (actor.getStage() == null) return;

        for (Actor stageActor : actor.getStage().getActors()) {
            if (stageActor instanceof TileActor) {
                TileActor tile = (TileActor) stageActor;

                if (overlaps(actor, tile)) {
                    // We treat FloorTile & PlatformTile as collidable
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();

                        // ----- Vertical Collision -----
                        // If falling (velocityY <= 0) and crossing the tile's top from above:
                        if (actor.velocityY <= 0f) {
                            float oldBottom = oldY;
                            float newBottom = actor.getY();
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                // Land on the tile
                                actor.setY(tileTop);
                                actor.velocityY = 0;
                                actor.isOnGround = true;
                            }
                        }

                        // ----- Horizontal Collision -----
                        float tileLeft  = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float newLeft   = actor.getX();
                        float newRight  = actor.getX() + actor.getWidth();

                        // If moving left (oldX > newX) and crossing tile's right edge
                        if (oldX > actor.getX() && oldX >= tileRight && newLeft < tileRight) {
                            actor.setX(tileRight);
                        }
                        // If moving right (oldX < newX) and crossing tile's left edge
                        else if (oldX < actor.getX() && (oldX + actor.getWidth()) <= tileLeft && newRight > tileLeft) {
                            actor.setX(tileLeft - actor.getWidth());
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility method to check if the actor's bounding box overlaps with a tile's bounding box.
     */
    private static boolean overlaps(PhysicalActor actor, TileActor tile) {
        Rectangle actorRect = new Rectangle(
            actor.getX(),
            actor.getY(),
            actor.getWidth(),
            actor.getHeight()
        );
        Rectangle tileRect  = new Rectangle(
            tile.getX(),
            tile.getY(),
            tile.getWidth(),
            tile.getHeight()
        );
        return actorRect.overlaps(tileRect);
    }

}
