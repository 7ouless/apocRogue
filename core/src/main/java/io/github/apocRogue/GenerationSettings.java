package io.github.apocRogue;

public class GenerationSettings {
    public int levelWidth  = 1080;    // total width of the level in pixels
    public int levelHeight = 720;     // total height of the level in pixels

    public float platformDensity = 5; // ~number of platforms per 1000px (approx)
    public float minGap = 100;        // min horizontal gap between platforms
    public float maxGap = 250;        // max horizontal gap
    public float heightVariance = 100;// vertical difference between subsequent platforms
    public float groundHeight = 50;   // thickness of the floor
    public float basePlatformHeight = 100; // starting height for the first platform
    public float platformWidth = 100; // base width for platforms
    public float platformWidthVariance = 30; // how much platform width can vary
    public float platformHeight = 20; // thickness of each platform
    public float minPlatformHeight = 20; // how high above ground the platform must be
    public boolean allowHazards = true;  // whether to randomly place hazards
    public float startX = 0f;           // optional offset for the first platform
}
