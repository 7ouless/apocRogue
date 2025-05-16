package io.github.apocRogue.actorAi.baseAI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.map.TileActor;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;

public class lineOfSight {

    public static boolean canSeeTarget(Actor self, Actor target, float sightRange, Stage stage) {
        if (stage == null) return false;

        //1) Distance check
        float sx = self.getX() + self.getWidth() / 2f;
        float sy = self.getY() + self.getHeight() / 2f;
        float tx = target.getX() + target.getWidth() / 2f;
        float ty = target.getY() + target.getHeight() / 2f;

        float dx = tx - sx;
        float dy = ty - sy;
        float dist2 = dx * dx + dy * dy;
        float range2 = sightRange * sightRange;
        if (dist2 > range2) {
            return false;
        }

        //2) Check if there's a clear line-of-sight (no blocking tile)
        return isLineClear(sx, sy, tx, ty, stage);
    }


    private static boolean isLineClear(float x1, float y1, float x2, float y2, Stage stage) {
        int steps = 15;
        float stepFrac = 1f / steps;

        for (int i = 1; i <= steps; i++) {
            float t = i * stepFrac;
            float sampleX = x1 + (x2 - x1) * t;
            float sampleY = y1 + (y2 - y1) * t;

            for (Actor actor : stage.getActors()) {
                if (actor instanceof TileActor) {
                    TileActor tile = (TileActor) actor;

                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float left = tile.getX();
                        float right = tile.getX() + tile.getWidth();
                        float bottom = tile.getY();
                        float top = tile.getY() + tile.getHeight();

                        if (sampleX >= left && sampleX <= right &&
                            sampleY >= bottom && sampleY <= top) {
                            // skip if sampleY is above tile's top by a small margin
                            if (sampleY < top + 1f) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
