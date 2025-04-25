package io.github.apocRogue.globals.difficulty;

public class CurrentDificulty {
    private static int difficulty = 3;

    public static int getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(int difficulty) {
        CurrentDificulty.difficulty = difficulty;
    }
}
