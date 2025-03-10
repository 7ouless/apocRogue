package io.github.apocRogue;

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

    private int tileWidth = settings.tileWidth;

    private float lastTileY = 0; //!!!when created should be assigned to the entrance's floor height!!!//

    private int xProgress = 0;
    private float yPos;

    private boolean platform = false;

    private int octaves = settings.octaves;  //#of octaves

    private List<TileInfo> platformTiles = new ArrayList<>();
    private List<TileInfo> dirtTiles = new ArrayList<>();
    private List<TileInfo> mapBorders;

    private ProcGen pg = new ProcGen();
    private Random random = new Random();

    public void generateMap(Stage stage) {
        mapBorders = createBorders(platformTiles, settings.roomWidth, settings.roomHeight);

        for (TileInfo info : mapBorders) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }

        while (xProgress < settings.roomWidth) {
            int x = generatePlatformLength();
            if (x + xProgress > settings.roomWidth) {
                x = settings.roomWidth - xProgress;
            }
            if (platform) {
                longPlatform(xProgress, Math.round(yPos), x); // xstart ystart width tileHeight
                platform = false;
            }
            else {
                yPos = joinGround(xProgress, Math.round(yPos), x, pg);
                platform = true;
            }
            xProgress += x;
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

    private float joinGround(int xStart, int yStart, int width, ProcGen pg) { //used to join the long platforms using procedurally generated terrain
        int seed = random.nextInt();
        pg.generatePermutationTable(seed);


        float persistence = 0.5f; //Amplitude "decay" for each octave
        float frequency = 0.1f; //Frequency for the first octave
        float noiseValue = 0;
        float amplitude = 1.0f;

        for (int i = 0; i < width / tileWidth; i++) {
            noiseValue = 0;
            for (int octave = 0; octave < octaves; octave++) {
                noiseValue += pg.noise(i * frequency) * amplitude;
                amplitude *= persistence;  // Reduce amplitude for each octave
                frequency *= 2.0f;  // Double the frequency for each octave
            }

            noiseValue += pg.noise(i * settings.smoothingFactor); //Scale input to smoothen noise
            int yPosition = (int) ((noiseValue + 1) / 2 * ((settings.groundMax - (settings.groundMax/6)) - settings.groundMin) + settings.groundMin);
            yPos = ProcGen.fitGrid(yPosition, tileWidth);
            if (lastTileY + tileWidth < yPos) { //if the tile is more than one tile spaces higher than the last tile
                int x = 1;
                while (lastTileY + (tileWidth * x) <= yPos - tileWidth) {
                    dirtTiles.add(new TileInfo((i * tileWidth) + xStart, lastTileY + (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            } else if (lastTileY - tileWidth > yPos) { //if the tile is more than one tile spaces lower than the last tile
                int x = 1;
                while (lastTileY - (tileWidth * x) > yPos) {
                    dirtTiles.add(new TileInfo((i * tileWidth - tileWidth) + xStart, lastTileY - (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
                    x++;
                }
            }

            //fills in the dirt blocks
            int j = 0;
            while (yPos - (tileWidth * j) >= settings.groundMin
            ) { //filling in the below tiles
                dirtTiles.add(new TileInfo((i * tileWidth) + xStart, yPos - (tileWidth * j), tileWidth, tileWidth, TileType.DIRT));
                j++;
            }
            lastTileY = yPos;

            platformTiles.add(new TileInfo(xStart + (tileWidth * i), yPos, tileWidth, tileWidth, TileType.PLATFORM));
        }
        return yPos;
    }

    private void longPlatform(int xStart, int yStart, int width) {
        for (int i = 0; i < width / tileWidth; i++) {
            platformTiles.add(new TileInfo(xStart + (i*tileWidth), yStart, tileWidth, tileWidth, TileType.PLATFORM));

            int j = settings.groundMin;
            while (j < yStart/tileWidth) {
                dirtTiles.add(new TileInfo(xStart + (i * tileWidth), j * tileWidth, tileWidth, tileWidth, TileType.DIRT));
                j++;
            }
        }
    }

    private int generatePlatformLength () {
        Random r = new Random();
        int x = r.nextInt(100);
        if (x < 10) {
            return tileWidth * 9;
        }
        else if (x < 30) {
            return tileWidth * 8;
        }
        else if (x < 50) {
            return tileWidth * 7;
        }
        else if (x < 80) {
            return tileWidth * 6;
        }
        else if (x < 85) {
            return tileWidth * 5;
        }
        else {
            return tileWidth * 4;
        }
    }
}
