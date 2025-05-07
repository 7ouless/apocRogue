package io.github.apocRogue.map;

public class TileInfo {
    public float x, y, width, height;
    public TileType type;
    public boolean flipX = false;  // default to false for all tiles
    public boolean useAltTexture = false;


    public TileInfo(float x, float y, float width, float height, TileType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public TileInfo(float x, float y, float width, float height, TileType type, boolean flipX, boolean useAltTexture) {
        this(x, y, width, height, type);
        this.flipX = flipX;
        this.useAltTexture = useAltTexture;
    }
}

