package io.github.apocRogue;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

public class ProceduralGenerator {
    // Optional: could maintain internal random or use MathUtils from LibGDX

    /**
     * Generate a list of TileInfo objects for platforms/hazards based on settings.
     */
    public List<TileInfo> generateMap(GenerationSettings cfg) {
        List<TileInfo> tiles = new ArrayList<>();

        float x = cfg.startX;                 // Starting X position
        float prevPlatformEndX = x;
        float prevY = cfg.basePlatformHeight; // Starting Y for the first platform
        int platformsCreated = 0;

        // Maximum number of platforms based on density
        int maxPlatforms = (int)(cfg.platformDensity * (cfg.levelWidth / 100f));

        while (x < cfg.levelWidth - cfg.minGap && platformsCreated < maxPlatforms) {
            // 1. Decide the width of this platform
            float platWidth = cfg.platformWidth;
            if (cfg.platformWidthVariance > 0) {
                platWidth += MathUtils.random(-cfg.platformWidthVariance, cfg.platformWidthVariance);
            }

            // 2. Horizontal gap from the previous platform
            float gap = MathUtils.random(cfg.minGap, cfg.maxGap);

            // For the very first platform, if we want a custom offset, use cfg.startX
            if (platformsCreated == 0 && cfg.startX > 0) {
                // do nothing special here unless you want
            } else {
                x = prevPlatformEndX + gap;
            }

            // If we exceed the level width, stop
            if (x + platWidth > cfg.levelWidth) break;

            // 3. Decide the Y position
            float newY = prevY + MathUtils.random(-cfg.heightVariance, cfg.heightVariance);
            // clamp it to remain within the level
            newY = MathUtils.clamp(
                newY,
                cfg.groundHeight + cfg.minPlatformHeight,
                cfg.levelHeight - cfg.minPlatformHeight
            );

            // Create a PLATFORM tile info
            tiles.add(new TileInfo(x, newY, platWidth, cfg.platformHeight, TileType.PLATFORM));
            platformsCreated++;

            // Possibly add a hazard on top
            if (cfg.allowHazards && MathUtils.random() < 0.2f) {
                float hazardX = x + platWidth/2 - 10;
                float hazardY = newY + cfg.platformHeight;
                tiles.add(new TileInfo(hazardX, hazardY, 20, 20, TileType.HAZARD));
            }

            // Update for the next loop
            prevPlatformEndX = x + platWidth;
            prevY = newY;
        }
        return tiles;
    }
}

