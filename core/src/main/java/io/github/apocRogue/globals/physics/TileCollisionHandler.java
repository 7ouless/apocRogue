package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.map.*;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.actors.mobs.CoreActor;
import io.github.apocRogue.map.PlatformTile;


public class TileCollisionHandler {


    private static final float BUFFER = 1f;


    public static void resolveCollisions(PhysicalActor actor, float oldX, float oldY) {
        if (actor.getStage() == null) return;

        final boolean ignorePlatforms = actor instanceof CoreActor;

        // Create actor's bounding box.
        Rectangle actorRect = new Rectangle(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());

        // Loop through all TileActor objects in the stage.
        for (Actor stageActor : actor.getStage().getActors()) {
            if (stageActor instanceof GrassOverlayTile || stageActor instanceof TreeTile || stageActor instanceof DecorTile|| stageActor instanceof PlatformGrassOverlayTile || (ignorePlatforms && stageActor instanceof PlatformTile)) {
                continue;
            }

            if (stageActor instanceof TileActor) {
                TileActor tile = (TileActor) stageActor;
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
                        if (stageActor instanceof EdgeTile || stageActor instanceof FloorTile) {
                            // Auto-step: climb up one standard tile
                            float step = ((TileActor) stageActor).getHeight();
                            actor.setY(actor.getY() + step);
                            actor.velocityY = 0f;
                            actor.isOnGround = true;
                        } else {
                            // Regular wall collision
                            actor.setX(tileRect.x - actorRect.width);
                        }
                    } else if (minOverlap == overlapRight) {
                        if (stageActor instanceof EdgeTile || stageActor instanceof FloorTile) {
                            // Auto-step: climb up one standard tile
                            float step = ((TileActor) stageActor).getHeight();
                            actor.setY(actor.getY() + step);
                            actor.velocityY = 0f;
                            actor.isOnGround = true;
                        } else {
                            // Regular wall collision
                            actor.setX(tileRect.x + tileRect.width);
                        }
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

