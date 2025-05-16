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

    //Tree Parameters
    public float treeDensity = 0.5f;
    public float treeWidth   = 400f;
    public float treeHeight  = 500f;
    public float treeYOffset =  20f;
    public int treeGap = 1;

    public float decorDensity   = 0.75f;
    public int   decorGap       = 0;

    //default “global” size (fallback)
    public float decorWidth     = 120f;
    public float decorHeight    = 100f;
    public float decorYOffset   = 35f;

    //per-variant overrides
    public float rockWidth      = 100f;
    public float rockHeight     =  70f;
    public float rockYOffset  =  45f;

    public float stoneWidth     = 70f;
    public float stoneHeight    =  40f;
    public float stoneYOffset =   45f;

    public float bushWidth      = 140f;
    public float bushHeight     = 90f;
    public float bushYOffset  =  40f;

    public float platformDensity;

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
