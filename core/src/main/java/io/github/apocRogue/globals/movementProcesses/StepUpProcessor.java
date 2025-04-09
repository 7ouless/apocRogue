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

    // Allow a little margin to step up near tile edges
    private static final float STEP_MARGIN = TILE_SIZE * 1.2f;

    // Allow stepping even if falling gently
    private static final float MAX_FALL_SPEED = -1000f;

    /**
     * Attempts to step the actor up onto an adjacent tile if that tile is one grid cell higher,
     * has no tile directly above it, and if there's horizontal overlap.
     */
    public static void attemptStepUp(PhysicalActor actor) {
        Stage stage = actor.getStage();
        if (stage == null) {
            Gdx.app.log("STEP_UP_DEBUG", "No stage found.");
            return;
        }

        // Don't step up if falling too fast
        if (actor.velocityY < MAX_FALL_SPEED) {
            Gdx.app.log("STEP_UP_DEBUG", "Too fast falling: " + actor.velocityY);
            return;
        }

        // Determine facing direction
        boolean isFacingRight = true;
        if (actor instanceof HasFacingDirection) {
            isFacingRight = ((HasFacingDirection) actor).isFacingRight();
        }

        // Use front of the actor for direction-based tile checking
        float actorFrontX = isFacingRight
            ? actor.getX() + actor.getWidth()
            : actor.getX();

        int targetCol = (int)(actorFrontX / TILE_SIZE);

        // Use base Y for accurate row checking
        int actorGridRow = (int)(actor.getY() / TILE_SIZE);
        int targetRow = actorGridRow + 1;

        Gdx.app.log("STEP_UP_DEBUG", "Checking step-up at (" + targetCol + ", " + targetRow + ")");

        // Don't step if there's a tile overhead
        if (getTileAt(targetCol, targetRow + 1, stage) != null) {
            Gdx.app.log("STEP_UP_DEBUG", "Tile overhead blocks stepping.");
            return;
        }

        // Get the tile we're trying to step onto
        TileActor candidateTile = getTileAt(targetCol, targetRow, stage);
        if (candidateTile != null) {
            float tileLeft = candidateTile.getX();
            float tileRight = tileLeft + candidateTile.getWidth();

            float actorLeft = actor.getX();
            float actorRight = actor.getX() + actor.getWidth();

            // ✅ Step up if overlapping horizontally
            boolean closeEnough = actorRight > tileLeft - STEP_MARGIN &&
                actorLeft < tileRight + STEP_MARGIN;

            if (closeEnough) {
                float newY = candidateTile.getY() + candidateTile.getHeight() - FOOT_OFFSET;

                // Skip if step is too tall
                float deltaY = Math.abs(newY - actor.getY());
                if (deltaY > TILE_SIZE * 1.5f) {
                    Gdx.app.log("STEP_UP_DEBUG", "Step too tall, skipping.");
                    return;
                }

                actor.stepTargetY = newY;
                actor.isSteppingUp = true;

                Gdx.app.log("STEP_UP_DEBUG", "Step-up triggered — actorLeft: " + actorLeft + ", actorRight: " + actorRight + ", tile [" + tileLeft + " to " + tileRight + "], newY = " + newY);
            } else {
                Gdx.app.log("STEP_UP_DEBUG", "Not close enough horizontally.");
            }
        } else {
            Gdx.app.log("STEP_UP_DEBUG", "No candidate tile found.");
        }
    }

    /**
     * Helper: Returns the TileActor at a given grid column and row in the stage, or null if none exists.
     */
    private static TileActor getTileAt(int col, int row, Stage stage) {
        Array<Actor> actors = stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor obj = actors.get(i);
            if (obj instanceof TileActor) {
                TileActor tile = (TileActor) obj;
                int tileCol = (int)(tile.getX() / TILE_SIZE);
                int tileRow = (int)(tile.getY() / TILE_SIZE);

                // Debug info
                Gdx.app.log("STEP_UP_DEBUG", "Found tile at (" + tileCol + ", " + tileRow + ") from pos (" + tile.getX() + ", " + tile.getY() + ")");

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
