package io.github.apocRogue.globals.movementProcesses;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;

public class StepUpProcessor {

    // Get the standard tile size from your ObstacleGetters or GenerationSettings.
    private static final float TILE_SIZE = ObstacleGetters.getStandardTileSize();

    /**
     * Attempts to step the actor up onto an adjacent tile if the tile in the next column
     * is exactly one tile high (i.e. there's only one tile, not two or more).
     *
     * @param actor The actor (player or enemy) to process.
     */
    public static void attemptStepUp(PhysicalActor actor) {
        if (actor.getStage() == null) return;

        // Determine the direction based on whether the actor is facing right.
        // For this example, assume the actor has a boolean field "facingRight".
        // (You might need to pass that in as a parameter or have a getter.)
        int direction = actor instanceof HasFacingDirection
            ? (((HasFacingDirection) actor).isFacingRight() ? 1 : -1)
            : 1; // default to right if not implemented

        int playerTileX = getActorTileX(actor);
        int playerTileY = getActorTileY(actor);
        int nextColumn = playerTileX + direction;

        int tileCount = 0;
        int highestTileY = -999;

        // Loop through all TileActor objects in the stage.
        for (Actor stageActor : actor.getStage().getActors()) {
            if (stageActor instanceof TileActor) {
                TileActor tile = (TileActor) stageActor;
                int tileX = (int)(tile.getX() / TILE_SIZE);
                int tileY = (int)(tile.getY() / TILE_SIZE);

                if (tileX == nextColumn && tileY >= playerTileY) {
                    tileCount++;
                    highestTileY = Math.max(highestTileY, tileY);
                }
            }
        }

        // If exactly one tile is found in that column and it's exactly one tile higher...
        if (tileCount == 1 && highestTileY == playerTileY + 1) {
            float tileTopPixels = (highestTileY + 1) * TILE_SIZE;
            actor.setY(tileTopPixels);
            actor.velocityY = 0f;
            actor.isOnGround = true;
            System.out.println("Stepped up one tile!");
        }
    }

    // Helper methods to compute the actor's grid position from its pixel position.
    private static int getActorTileX(PhysicalActor actor) {
        // Use the actor's center x for a more robust calculation.
        return (int)((actor.getX() + actor.getWidth() / 2f) / TILE_SIZE);
    }
    public interface HasFacingDirection {
        boolean isFacingRight();
    }
    private static int getActorTileY(PhysicalActor actor) {
        // Assume the actor's bottom (getY()) is its foot.
        return (int)(actor.getY() / TILE_SIZE);
    }
}
