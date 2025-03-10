package io.github.apocRogue.map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds either a procedural or predefined set of tiles (including the floor).
 */
public class MapManager {

    public GenerationSettings settings = GenerationType.PLAINS.settings;

    private float lastTileY; //!!!when created should be assigned to the entrance's floor height!!!//

    int octaves;  //#of octaves
    float persistence = 0.5f; //Amplitude "decay" for each octave
    float frequency = 0.05f; //Frequency for the first octave

    public void generateMap(Stage stage) {
        ProcGen pg = new ProcGen();
        List<TileInfo> platformTiles = new ArrayList<>();
        List<TileInfo> dirtTiles = new ArrayList<>();
        List<TileInfo> mapBorders;

        Random random = new Random();
        int seed = random.nextInt();
        pg.generatePermutationTable(seed);

        int levelWidth = settings.roomWidth;
        float noiseValue = 0;
        float amplitude = 1.0f;
        float freq = frequency;
        octaves = settings.octaves;
        int tileWidth = settings.tileWidth;
        for (int i = 0; i < levelWidth / tileWidth; i++) {
            for (int octave = 0; octave < octaves; octave++) {
                noiseValue += pg.noise(i * freq) * amplitude;
                amplitude *= persistence;  // Reduce amplitude for each octave
                freq *= 2.0f;  // Double the frequency for each octave
            }

            noiseValue = pg.noise(i * settings.smoothingFactor); //Scale input to smoothen noise
            int yPosition = (int) ((noiseValue + 1) / 2 * (settings.groundMax - settings.groundMin) + settings.groundMin);

            float yPos = ProcGen.fitGrid(yPosition, tileWidth);
            if (lastTileY + tileWidth < yPos) { //if the tile is more than one tile spaces higher than the last tile
                int x = 1;
                while (lastTileY + (tileWidth * x) <= yPos - tileWidth) {
                    dirtTiles.add(new TileInfo(i * tileWidth, lastTileY + (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            }
            else if (lastTileY - tileWidth > yPos) { //if the tile is more than one tile spaces lower than the last tile
                int x = 1;
                while (lastTileY - (tileWidth * x) > yPos) {
                    dirtTiles.add(new TileInfo(i * tileWidth - tileWidth, lastTileY - (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            }
            //fills in the dirt blocks
            int j = 1;
            while (yPos - (tileWidth * j) >= settings.groundMin - settings.tileWidth
            ) { //filling in the below tiles
                dirtTiles.add(new TileInfo(i * tileWidth, yPos - (tileWidth * j), tileWidth, tileWidth, TileType.DIRT));
                j++;
            }
            lastTileY = yPos;

            platformTiles.add(new TileInfo(i * tileWidth, yPos, tileWidth, tileWidth, TileType.PLATFORM));
        }

        mapBorders = createBorders(settings.roomWidth, settings.roomHeight);

        for (TileInfo info : mapBorders) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }

        for (TileInfo info : dirtTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }

        for (TileInfo info : platformTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }
    }

    private List<TileInfo> createBorders(int w, int h) {
        ArrayList<TileInfo> tiles = new ArrayList<>();
        int i = 0;
        while (i < w/settings.tileWidth) { //horizontal tiles
            tiles.add(new TileInfo(i*settings.tileWidth, settings.roomHeight, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            tiles.add(new TileInfo(i*settings.tileWidth, 0, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            i++;
        }
        i = 0;
        while (i <= h/ settings.tileWidth) { //vertical tiles
            tiles.add(new TileInfo(-settings.tileWidth, i*settings.tileWidth, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            tiles.add(new TileInfo(settings.roomWidth, i* settings.tileWidth, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            i++;
        }
        return tiles;
    }

    private Actor createTileActor(TileInfo info) {
        switch (info.type) {
            case GROUND:
                return new FloorTile(info.x, info.y, info.width, info.height);
            case HAZARD:
                return new HazardTile(info.x, info.y, info.width, info.height);
            case DIRT:
                return new DirtTile(info.x, info.y, info.width, info.height);
            default: // PLATFORM
                return new PlatformTile(info.x, info.y, info.width, info.height);
        }
    }
}
