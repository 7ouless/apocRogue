package io.github.apocRogue.map;

public class MapConfig {
    public int levelWidth = 3000;   // Total width of the level/world
    public int levelHeight = 720;   // Total height of the level/world

    public int floorHeight = 50;    // Height of the floor at the bottom
    public int platformCount = 10;  // How many floating platforms to generate

    public int minPlatformWidth = 100;
    public int maxPlatformWidth = 300;
    public int platformHeight = 20;

    public int minPlatformY = 100;  // Minimum Y for floating platforms
    public int maxPlatformY = 600;  // Maximum Y for floating platforms
}
