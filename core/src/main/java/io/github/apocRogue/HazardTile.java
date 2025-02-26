package io.github.apocRogue;

import com.badlogic.gdx.graphics.Color;

// Hazardous tile (e.g., spikes)
public class HazardTile extends TileActor {
    public HazardTile(float x, float y, float width, float height) {
        super(x, y, width, height, Color.SCARLET); // red color to signify danger
    }
}
