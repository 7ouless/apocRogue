package io.github.apocRogue;

public enum GenerationType {
    PLAINS(new GenerationSettings(300, 50, 0, 0, 20, 0.020F)),
    SMOOTHHILLS(new GenerationSettings(700, 50, 0, 0, 5, 0.75F)),
    JAGGEDHILLS(new GenerationSettings(500, 50, 0, 0, 5, 0.75F));

    public final GenerationSettings settings;

    GenerationType(GenerationSettings settings) {
        this.settings = settings;
    }
}
