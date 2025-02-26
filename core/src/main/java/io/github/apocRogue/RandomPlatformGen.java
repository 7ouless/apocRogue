package io.github.apocRogue;

import static com.badlogic.gdx.math.MathUtils.random;

public class RandomPlatformGen {
    private int width;
    private int height;
    private int platformStartX;
    private int platformStartY;
    public RandomPlatformGen(int width, int height) {
        this.width = width;
        this.height = height;
    }
    private void RandomStart(){
        platformStartX = random(15,width-15);
        platformStartY = random(150, height-150);
    }

}

