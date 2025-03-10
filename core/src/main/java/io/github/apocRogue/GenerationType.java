package io.github.apocRogue;

public enum GenerationType {
    PLAINS(new GenerationSettings(700, 0, 0, 0, 3, 0.10F, 2000, 750, 50)),
    SMOOTHHILLS(new GenerationSettings(700, 0, 0, 0, 5, 0.1F, 2000, 750, 25)),
    JAGGEDHILLS(new GenerationSettings(500, 0, 0, 0, 5, 0.75F, 2000, 750, 25));

    public final GenerationSettings settings;

    GenerationType(GenerationSettings settings) {
        this.settings = settings;
    }
}
