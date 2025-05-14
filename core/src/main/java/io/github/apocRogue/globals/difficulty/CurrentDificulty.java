package io.github.apocRogue.globals.difficulty;

public class CurrentDificulty {
    private static int difficulty = 3;
    private static int radiation = 1;

    private static int skullLevel = 1;
    private static int worldLevel = 1;

    public static int getSkullLevel() {
        return skullLevel;
        }

    public static int getWorldLevel() {
        return worldLevel;
        }

    public static void setRunLevels(int skulls, int worlds) {
        skullLevel = skulls;
        worldLevel = worlds;
        }

    public static float getRadiationChance() {
        switch (radiation) {
            case 1: return 0.2f;
            case 2: return 0.5f;
            case 3: return 0.8f;
            default: return 0f;
        }
    }

    public static int getDifficulty() {
        return difficulty;
    }

    public static int getRadiation() { return radiation; }
    public static void setRadiation(int r) { radiation = r; }

    public static void setDifficulty(int difficulty) {
        CurrentDificulty.difficulty = difficulty;
    }
}
