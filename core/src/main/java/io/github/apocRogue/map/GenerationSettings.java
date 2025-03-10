package io.github.apocRogue.map;

public class GenerationSettings {
    public int roomWidth;
    public int roomHeight;
    public int dummy1;
    public int dummy2;
    public int tileWidth;
    public float smoothingFactor;
    public int octaves;
    public int groundMax;
    public int groundMin;

    // If you need platformDensity, add it:
    public float platformDensity;

    // 1) A default constructor (no arguments)
    public GenerationSettings() {
        // Provide sensible defaults:
        this.roomWidth = 3000;
        this.roomHeight = 720;
        this.tileWidth = 25;
        this.smoothingFactor = 0.02f;
        this.octaves = 2000;
        this.groundMax = 750;
        this.groundMin = 50;
        this.platformDensity = 5;  // If you plan to use it
    }

    // 2) The existing 9-arg constructor
    public GenerationSettings(
        int roomWidth,
        int roomHeight,
        int dummy1,
        int dummy2,
        int tileWidth,
        float smoothingFactor,
        int octaves,
        int groundMax,
        int groundMin
    ) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.dummy1 = dummy1;
        this.dummy2 = dummy2;
        this.tileWidth = tileWidth;
        this.smoothingFactor = smoothingFactor;
        this.octaves = octaves;
        this.groundMax = groundMax;
        this.groundMin = groundMin;
    }
}
