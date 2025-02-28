package io.github.apocRogue;

public enum GenerationType {
    PLAINS(new GenerationSettings(300, 50, 2, 6, 5, 0.020F));

    public final GenerationSettings settings;

    GenerationType(GenerationSettings settings) {
        this.settings = settings;
    }
}
