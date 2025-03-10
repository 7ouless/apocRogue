package io.github.apocRogue.map;

import java.util.Random;

public class ProcGen {

    private static final int SIZE = 256;
    private static int[] permutation = new int[SIZE * 2];

    public static void generatePermutationTable(long seed) {
        Random random = new Random(seed);
        int[] p = new int[SIZE];

        // Fill array with values 0 to 255
        for (int i = 0; i < SIZE; i++) {
            p[i] = i;
        }

        //Implementing Fisher+Yates algorithm to randomise the values
        for (int i = SIZE - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // Swap p[i] and p[j]
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        //Copy permutation table twice to avoid overflow issues
        for (int i = 0; i < SIZE; i++) {
            permutation[i] = p[i];
            permutation[i + SIZE] = p[i]; // Duplicate values for indexing
        }
    }

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float grad(int hash, float x) {
        return (hash & 1) == 0 ? x : -x;
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    public float noise(float x) {
        int X = (int) Math.floor(x) & 255;  //Getting integer position
        float xf = x - (float) Math.floor(x);  //Getting the fractional position
        float u = fade(xf);  //Smoothing Function

        int g0 = permutation[X] & 15;  //Gradient hash for left point
        int g1 = permutation[X + 1] & 15;  //Gradient hash for right point

        float grad0 = grad(g0, xf);  //Compute gradient at left point
        float grad1 = grad(g1, xf - 1); //Compute gradient at right point

        return lerp(u, grad0, grad1);  //Interpolate between gradients
    }
}
