package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.map.GrassOverlayTile;


public class TileCollisionHandler {

    // Optionally, add a small buffer to make collisions extra sensitive.
    private static final float BUFFER = 1f;

    /**
     * Resolves collisions between a physical actor and any TileActor in the stage.
     * If any overlap is found, the actor is pushed out along the axis of smallest overlap.
     *
     * @param actor The physical actor (e.g., PlayerActor, EnemyActor).
     * @param oldX  The actor's X position before movement.
     * @param oldY  The actor's Y position before movement.
     */
    public static void resolveCollisions(PhysicalActor actor, float oldX, float oldY) {
        if (actor.getStage() == null) return;

        // Create actor's bounding box.
        Rectangle actorRect = new Rectangle(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());

        // Loop through all TileActor objects in the stage.
        for (Actor stageActor : actor.getStage().getActors()) {
            if (stageActor instanceof io.github.apocRogue.map.GrassOverlayTile) {
                continue;    // never collide with grass
            }
            if (stageActor instanceof TileActor) {
                TileActor tile = (TileActor)stageActor;
                // Inflate the tile rectangle slightly for extra sensitivity.
                Rectangle tileRect = new Rectangle(tile.getX() - BUFFER, tile.getY() - BUFFER,
                    tile.getWidth() + 2 * BUFFER, tile.getHeight() + 2 * BUFFER);

                if (actorRect.overlaps(tileRect)) {
                    // Compute overlaps on all four sides.
                    float overlapLeft = (actorRect.x + actorRect.width) - tileRect.x;
                    float overlapRight = (tileRect.x + tileRect.width) - actorRect.x;
                    float overlapBottom = (actorRect.y + actorRect.height) - tileRect.y;
                    float overlapTop = (tileRect.y + tileRect.height) - actorRect.y;

                    // Find the minimum overlap.
                    float minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                        Math.min(overlapBottom, overlapTop));

                    // Resolve collision along that axis.
                    if (minOverlap == overlapLeft) {
                        // Push actor left.
                        actor.setX(tileRect.x - actorRect.width);
                    } else if (minOverlap == overlapRight) {
                        // Push actor right.
                        actor.setX(tileRect.x + tileRect.width);
                    } else if (minOverlap == overlapBottom) {
                        // Push actor down.
                        actor.setY(tileRect.y - actorRect.height);
                        actor.velocityY = 0f;
                        actor.isOnGround = true;
                    } else if (minOverlap == overlapTop) {
                        // Push actor up.
                        actor.setY(tileRect.y + tileRect.height);
                        actor.velocityY = 0f;
                        actor.isOnGround = true;
                    }

                    // Update the actor's bounding rectangle.
                    actorRect.setPosition(actor.getX(), actor.getY());
                }
            }
        }
    }
}
