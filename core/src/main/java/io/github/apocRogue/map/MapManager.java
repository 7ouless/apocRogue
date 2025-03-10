package io.github.apocRogue.map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds either a procedural or predefined set of tiles (including the floor).
 */
public class MapManager {

    private int tileWidth = 25;
    GenerationSettings gs;

    int octaves = 2000;  //#of octaves
    float persistence = 0.5f; //Amplitude "decay" for each octave
    float frequency = 0.05f; //Frequency for the first octave

    public void generateMap(Stage stage) {
        ProcGen pg = new ProcGen();
        List<TileInfo> platformTiles = new ArrayList<>();
        GenerationSettings gs = new GenerationSettings();

        pg.generatePermutationTable(275937494);

        for (int i = 0; i < gs.levelWidth/tileWidth; i++) {
            float noiseValue = 0;
            float amplitude = 1.0f;
            float freq = frequency;

            for (int octave = 0; octave < octaves; octave++) {
                noiseValue += pg.noise(i * freq) * amplitude;
                amplitude *= persistence;  // Reduce amplitude for each octave
                freq *= 2.0f;  // Double the frequency for each octave
            }

            noiseValue = pg.noise(i * 0.2f); //Scale input to smoothen noise
            int yPosition = (int) ((noiseValue + 1) / 2 * (gs.maxPlatHeight - gs.minPlatHeight) + gs.minPlatHeight);

            platformTiles.add(new TileInfo(i * tileWidth, yPosition, tileWidth, tileWidth, TileType.PLATFORM));
        }

        for (TileInfo info : platformTiles) {
            Actor tileActor = createTileActor(info);
            System.out.println(info.y);
            stage.addActor(tileActor);
        }

        // Also add a base floor
        stage.addActor(new FloorTile(0, 0, 1080, 50));
    }

    private Actor createTileActor(TileInfo info) {
        switch (info.type) {
            case GROUND:
                return new FloorTile(info.x, info.y, info.width, info.height);
            case HAZARD:
                return new HazardTile(info.x, info.y, info.width, info.height);
            default: // PLATFORM
                return new PlatformTile(info.x, info.y, info.width, info.height);
        }
    }

    // Example layouts
    private List<TileInfo> createLayout1() {
        List<TileInfo> layout = new ArrayList<>();
        layout.add(new TileInfo(0, 0, gs.levelWidth, 50, TileType.GROUND));
        layout.add(new TileInfo(200, 120, 100, 20, TileType.PLATFORM));
        layout.add(new TileInfo(400, 200, 120, 20, TileType.PLATFORM));
        layout.add(new TileInfo(600, 50, 30, 30, TileType.HAZARD));
        return layout;
    }
}
