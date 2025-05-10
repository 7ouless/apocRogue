package io.github.apocRogue.map;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.globals.getters.worldGetters;

public class GenerationSettings {
    public int roomWidth;
    public int roomHeight;
    public int dummy1;
    public int dummy2;
    public int tileWidth;
    public int tileHeight;
    public int octaves;
    public int groundMax;
    public int groundMin;

    public float treeDensity = 0.4f;

    public float treeWidth   = 300f;
    public float treeHeight  = 400f;
    public float treeYOffset =  20f;

    public float platformDensity;

    // 1) A default constructor (no arguments)
    public GenerationSettings() {
        // Provide sensible defaults:
        this.roomWidth = worldGetters.getWorldOneWidth();
        this.roomHeight = worldGetters.getWorldOneHeight();
        this.tileWidth = ObstacleGetters.getStandardTileSize();
        this.octaves = 2000;
        this.groundMax = 750;
        this.groundMin = 50;
        this.platformDensity = 5;
    }

    // 2) The existing 9-arg constructor
    public GenerationSettings(
        int roomWidth,
        int roomHeight,
        int dummy1,
        int dummy2,
        int tileWidth,
        int tileHeight,
        int octaves,
        int groundMax,
        int groundMin
    ) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.dummy1 = dummy1;
        this.dummy2 = dummy2;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.octaves = octaves;
        this.groundMax = groundMax;
        this.groundMin = groundMin;
    }
}
