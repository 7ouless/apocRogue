package io.github.apocRogue.map;

public enum GenerationType {
    PLAINS(new GenerationSettings(3000, 50, 0, 0, 20, 0.020F, 2000, 750, 50)),
    SMOOTHHILLS(new GenerationSettings(700, 50, 0, 0, 5, 0.1F, 2000, 750, 25)),
    JAGGEDHILLS(new GenerationSettings(500, 50, 0, 0, 5, 0.75F, 2000, 750, 25));

    public final GenerationSettings settings;

    GenerationType(GenerationSettings settings) {
        this.settings = settings;
    }
}
