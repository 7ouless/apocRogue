package io.github.apocRogue.globals.movementProcesses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;

public class StepUpProcessor {

    // Use the standard tile size from your settings.
    private static final int TILE_SIZE = ObstacleGetters.getStandardTileSize(); // e.g., 25 pixels
    // Adjust this if your sprite's getY() is not exactly at the feet.
    private static final float FOOT_OFFSET = 24.0f;

    /**
     * Attempts to step the actor up onto an adjacent tile if that tile is one grid cell higher,
     * has no tile directly above it, and if the actor’s center is within its horizontal bounds.
     * This method only triggers if the actor is not falling.
     */
    public static void attemptStepUp(PhysicalActor actor) {
        Stage stage = actor.getStage();
        if (stage == null) return;

        // Do not step up if the actor is falling.
        if (actor.velocityY < 0) return;

        // Determine facing direction.
        boolean isFacingRight = true;
        if (actor instanceof HasFacingDirection) {
            isFacingRight = ((HasFacingDirection) actor).isFacingRight();
        }

        // Compute the actor's grid column using its horizontal center.
        float actorCenterX = actor.getX() + actor.getWidth() / 2f;
        int actorGridCol = (int)(actorCenterX / TILE_SIZE);
        // Target column is one cell in the facing direction.
        int targetCol = actorGridCol + (isFacingRight ? 1 : -1);

        // Compute the actor's grid row using its feet (adjusted by FOOT_OFFSET).
        float actorFeetY = actor.getY() + FOOT_OFFSET;
        int actorGridRow = (int)(actorFeetY / TILE_SIZE);
        // Candidate tile should be exactly one cell higher.
        int targetRow = actorGridRow + 1;

        // Check overhead: if there's a tile directly above the candidate tile, don't step up.
        if (getTileAt(targetCol, targetRow + 1, stage) != null) return;

        // Get the candidate tile at (targetCol, targetRow)
        TileActor candidateTile = getTileAt(targetCol, targetRow, stage);
        if (candidateTile != null) {
            // For full horizontal collision, check that the actor's center lies within the candidate tile.
            float tileLeft = candidateTile.getX();
            float tileRight = tileLeft + candidateTile.getWidth();
            if (actorCenterX >= tileLeft && actorCenterX <= tileRight) {
                // Step up: align actor's feet with the tile's top.
                float newY = candidateTile.getY() + candidateTile.getHeight() - FOOT_OFFSET;
                actor.setY(newY);
                actor.velocityY = 0f;
                actor.isOnGround = true;
                Gdx.app.log("STEP_UP_DEBUG", "Stepped up to Y: " + newY);
            }
        }
    }

    /**
     * Helper: Returns the TileActor at a given grid column and row in the stage, or null if none exists.
     * Uses index-based iteration to avoid nested iterators.
     */
    private static TileActor getTileAt(int col, int row, Stage stage) {
        Array<Actor> actors = stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor obj = actors.get(i);
            if (obj instanceof TileActor) {
                TileActor tile = (TileActor) obj;
                int tileCol = (int)(tile.getX() / TILE_SIZE);
                int tileRow = (int)(tile.getY() / TILE_SIZE);
                if (tileCol == col && tileRow == row) {
                    return tile;
                }
            }
        }
        return null;
    }

    public interface HasFacingDirection {
        boolean isFacingRight();
    }
}
