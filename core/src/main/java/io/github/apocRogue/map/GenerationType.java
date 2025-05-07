package io.github.apocRogue.map;

public enum GenerationType {
    PLAINS(new GenerationSettings(6000, 1500, 0, 0, 400, 50, 1, 700, 0)),
    SMOOTHHILLS(new GenerationSettings(700, 50, 0, 0, 200, 25, 20, 750, 0)),
    JAGGEDHILLS(new GenerationSettings(500, 50, 0, 0, 200, 25, 20, 750, 0));

    public final GenerationSettings settings;

    GenerationType(GenerationSettings settings) {
        this.settings = settings;
    }
}
