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

    private boolean changeCalculated = false;
    private int heightDifference;

    private int tileWidth = settings.tileWidth;

    private float lastTileY = 0; //!!!when created should be assigned to the entrance's floor height!!!//

    private int xProgress = 0;
    private float yPos = 300;

    private boolean platform = false;

    private int octaves = settings.octaves;  //#of octaves

    private List<TileInfo> platformTiles = new ArrayList<>();
    private List<TileInfo> dirtTiles = new ArrayList<>();
    private List<TileInfo> mapBorders;

    private final ProcGen pg = new ProcGen();
    private final Random random = new Random();

    public void generateMap(Stage stage) {
        mapBorders = createBorders(platformTiles, settings.roomWidth, settings.roomHeight);
        createGround();

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

    private List<TileInfo> createBorders(List<TileInfo> tiles, int w, int h) {
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

    private void createGround() { //used to join the long platforms using procedurally generated terrain
        int seed = random.nextInt(99999999);
        pg.generatePermutationTable(seed);

        float persistence = .5f; //Amplitude "decay" for each octave
        float frequency = 0.5f; //Frequency for the first octave
        float noiseValue = 0;
        float amplitude = 0.9f;

        for (int i = 0; i < settings.roomWidth / tileWidth; i++) {
            noiseValue = 0;
            for (int octave = 0; octave < octaves; octave++) {
                noiseValue += pg.noise(i * frequency) * amplitude;
                amplitude *= persistence;  // Reduce amplitude for each octave
                frequency *= 1.15f;  // Double the frequency for each octave
            }

            noiseValue += pg.noise(i * 0.025f); //Scale input to smoothen noise
            int yPosition = (int) ((noiseValue + 1) / 2 * ((settings.groundMax - (settings.groundMin)) - settings.groundMin) + settings.groundMin);
            yPos = ProcGen.fitGrid(yPosition, tileWidth);

            if (lastTileY + tileWidth < yPos) { //if the tile is more than one tile spaces higher than the last tile
                int x = 1;
                while (lastTileY + (tileWidth * x) <= yPos - tileWidth) {
                    dirtTiles.add(new TileInfo((i * tileWidth), lastTileY + (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            } else if (lastTileY - tileWidth > yPos) { //if the tile is more than one tile spaces lower than the last tile
                int x = 1;
                while (lastTileY - (tileWidth * x) > yPos) {
                    dirtTiles.add(new TileInfo((i * tileWidth - tileWidth), lastTileY - (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            }

            //fills in the dirt blocks
            int j = 0;
            while (yPos - (tileWidth * j) >= settings.groundMin
            ) { //filling in the below tiles
                dirtTiles.add(new TileInfo(i * tileWidth, yPos - (tileWidth * j), tileWidth, tileWidth, TileType.DIRT));
                j++;
            }
            lastTileY = yPos;

            platformTiles.add(new TileInfo(tileWidth * i, yPos, tileWidth, tileWidth, TileType.PLATFORM));
        }
    }
}
