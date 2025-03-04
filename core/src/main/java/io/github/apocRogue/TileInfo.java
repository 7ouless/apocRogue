package io.github.apocRogue;

public class TileInfo {
    public float x, y, width, height;
    public TileType type;

    public TileInfo(float x, float y, float width, float height, TileType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }
    // If you want to keep a 4-argument constructor, that’s fine,
    // but your code references the 5-argument one, so that must exist.

}
