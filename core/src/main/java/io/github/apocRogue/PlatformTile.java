package io.github.apocRogue;

import com.badlogic.gdx.graphics.Color;

// Floating platform tile
public class PlatformTile extends TileActor {
    public PlatformTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.FOREST);  // e.g., use a greenish color for platforms
    }
}
