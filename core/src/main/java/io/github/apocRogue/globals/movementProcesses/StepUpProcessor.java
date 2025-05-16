package io.github.apocRogue.globals.movementProcesses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;

public class StepUpProcessor {

    private static final int TILE_SIZE = ObstacleGetters.getStandardTileSize(); // e.g., 25 pixels
    private static final float FOOT_OFFSET = 24.0f;

    public static void attemptStepUp(PhysicalActor actor) {
        Stage stage = actor.getStage();
        if (stage == null) return;

        //stop stepping up if the player is falling
        if (actor.velocityY < 0) return;

        boolean isFacingRight = true;
        if (actor instanceof HasFacingDirection) {
            isFacingRight = ((HasFacingDirection) actor).isFacingRight();
        }

        float actorCenterX = actor.getX() + actor.getWidth() / 2f;
        int actorGridCol = (int)(actorCenterX / TILE_SIZE);
        int targetCol = actorGridCol + (isFacingRight ? 1 : -1);

        //copmuting players grid row
        float actorFeetY = actor.getY() + FOOT_OFFSET;
        int actorGridRow = (int)(actorFeetY / TILE_SIZE);
        // Candidate tile should be exactly one cell higher.
        int targetRow = actorGridRow + 1;

        //stops stepping up if block is overhead
        if (getTileAt(targetCol, targetRow + 1, stage) != null) return;

        TileActor candidateTile = getTileAt(targetCol, targetRow, stage);
        if (candidateTile != null) {
            float tileLeft = candidateTile.getX();
            float tileRight = tileLeft + candidateTile.getWidth();
            if (actorCenterX >= tileLeft && actorCenterX <= tileRight) {
                float newY = candidateTile.getY() + candidateTile.getHeight() - FOOT_OFFSET;
                actor.setY(newY);
                actor.velocityY = 0f;
                actor.isOnGround = true;
                Gdx.app.log("STEP_UP_DEBUG", "Stepped up to Y: " + newY);
            }
        }
    }

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
