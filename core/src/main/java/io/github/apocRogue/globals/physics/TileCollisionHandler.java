package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.map.*;
import io.github.apocRogue.globals.getters.ObstacleGetters;


public class TileCollisionHandler {


    private static final float BUFFER = 1f;


    public static void resolveCollisions(PhysicalActor actor, float oldX, float oldY) {
        if (actor.getStage() == null) return;

        //creating actors bounding box
        Rectangle actorRect = new Rectangle(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());

        //loops through all TileActor objects in the stage
        for (Actor stageActor : actor.getStage().getActors()) {
            if (stageActor instanceof GrassOverlayTile || stageActor instanceof TreeTile || stageActor instanceof DecorTile|| stageActor instanceof PlatformGrassOverlayTile) {
                continue;
            }
            if (stageActor instanceof TileActor) {
                TileActor tile = (TileActor)stageActor;
                Rectangle tileRect = new Rectangle(tile.getX() - BUFFER, tile.getY() - BUFFER,
                    tile.getWidth() + 2 * BUFFER, tile.getHeight() + 2 * BUFFER);

                if (actorRect.overlaps(tileRect)) {
                    float overlapLeft = (actorRect.x + actorRect.width) - tileRect.x;
                    float overlapRight = (tileRect.x + tileRect.width) - actorRect.x;
                    float overlapBottom = (actorRect.y + actorRect.height) - tileRect.y;
                    float overlapTop = (tileRect.y + tileRect.height) - actorRect.y;

                    //min overlap
                    float minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                        Math.min(overlapBottom, overlapTop));

                    //collision resolution
                    if (minOverlap == overlapLeft) {
                        if (stageActor instanceof EdgeTile || stageActor instanceof FloorTile) {
                                       //auto-step - climb up one standard tile
                                           float step = ((TileActor)stageActor).getHeight();
                                       actor.setY(actor.getY() + step);
                                       actor.velocityY = 0f;
                                       actor.isOnGround = true;
                                    } else {
                                        //a regular wall collision
                                            actor.setX(tileRect.x - actorRect.width);
                                    }
                    } else if (minOverlap == overlapRight) {
                        if (stageActor instanceof EdgeTile || stageActor instanceof FloorTile ) {
                            float step = ((TileActor)stageActor).getHeight();
                                       actor.setY(actor.getY() + step);
                                       actor.velocityY = 0f;
                                       actor.isOnGround = true;
                                   } else {
                                           actor.setX(tileRect.x + tileRect.width);
                                   }
                    } else if (minOverlap == overlapBottom) {
                        //actor down
                        actor.setY(tileRect.y - actorRect.height);
                        actor.velocityY = 0f;
                        actor.isOnGround = true;
                    } else if (minOverlap == overlapTop) {
                        //actor up
                        actor.setY(tileRect.y + tileRect.height);
                        actor.velocityY = 0f;
                        actor.isOnGround = true;
                    }

                    //update bounding rectangle
                    actorRect.setPosition(actor.getX(), actor.getY());
                }
            }
        }
    }
}
