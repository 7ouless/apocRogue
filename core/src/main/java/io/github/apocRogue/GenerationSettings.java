package io.github.apocRogue;

public class GenerationSettings {
    public int groundMax; //Maximum Y-Axis a tile should reach
    public int groundMin; //Minimum Y-Axis a tile should reach
    public int repeatFlatLimitMax; //Maximum number of times a tile should be repeated before the noise generator is called again
    public int repeatFlatLimitMin; //Minimum number of times a tile should be repeated before the noise generator is called again
    public int octaves; //Different repetitions of the generator (more of them equals a smoother output)
    public float smoothingFactor;
    public int roomWidth;
    public int roomHeight;
    public int tileWidth;
    public GenerationSettings(int groundMax, int groundMin, int repeatFlatLimitMin, int repeatFlatLimitMax, int octaves, float smoothingFactor, int roomWidth, int roomHeight, int tileWidth) {
        this.groundMax = groundMax;
        this.groundMin = groundMin;
        this.repeatFlatLimitMin = repeatFlatLimitMin;
        this.repeatFlatLimitMax = repeatFlatLimitMax;
        this.octaves = octaves;
        this.smoothingFactor = smoothingFactor;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.tileWidth = tileWidth;
    }
}
