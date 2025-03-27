package io.github.apocRogue.map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds either a procedural or predefined set of tiles (including the floor).
 */
public class RoomCreator {

    public static GenerationSettings settings = GenerationType.PLAINS.settings;

    private boolean canCreateIsland = false;
    private boolean creatingIsland = false;
    private int canCreateIslandCount = 0;
    private int islandGoalLength;
    private int islandCurrentLength;
    private float islandCurrentY;
    private float islandHeight;

    private int EEHEIGHT = 5;
    private int EEWIDTH = 12;
    private int EEYLevel;

    private int tileWidth = settings.tileWidth;

    private float lastTileY = 0; //!!!when created should be assigned to the entrance's floor height!!!//

    private float islandYPos = 600;
    private float yPos = 300;

    private boolean platform = false;

    private int octaves = settings.octaves;  //#of octaves

    private List<TileInfo> platformTiles = new ArrayList<>();
    private List<TileInfo> dirtTiles = new ArrayList<>();

    private final ProcGen pg = new ProcGen();
    private final Random random = new Random();

    public void generateMap(Stage stage) { //adding chest spawns and mob spawns later as parameters to be used in later created methods
        createRoom();

        for (TileInfo info : dirtTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }

        for (TileInfo info : platformTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }
    }

    private void createRoom() {
        createGround();
        createBorders(platformTiles);
    }

    private void createBorders(List<TileInfo> tiles) {
        EEYLevel = random.nextInt(EEYLevel, settings.roomHeight - ((EEHEIGHT + 1) * tileWidth));
        EEYLevel = (int) pg.fitGrid(EEYLevel, tileWidth);

        int i = 0;
        while (i < settings.roomWidth/settings.tileWidth) { //horizontal tiles
            tiles.add(new TileInfo(i*settings.tileWidth, settings.roomHeight, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            tiles.add(new TileInfo(i*settings.tileWidth, 0, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
            i++;
        }
        i = 0;
        while (i <= settings.roomHeight/ settings.tileWidth) { //vertical tiles
            if (i * tileWidth < EEYLevel || i * tileWidth > EEYLevel) {
                tiles.add(new TileInfo(-settings.tileWidth, i * settings.tileWidth, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                tiles.add(new TileInfo(settings.roomWidth, i * settings.tileWidth, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                i++;
            }
            else {
                for (int x = 0; x < EEWIDTH; x++) {
                    //Adding the horizontal 'bars' of the entrance and exit
                    tiles.add(new TileInfo(settings.roomWidth + (x * tileWidth), EEYLevel + (EEHEIGHT * tileWidth), settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                    tiles.add(new TileInfo(settings.roomWidth + (x * tileWidth), EEYLevel, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                    tiles.add(new TileInfo(-settings.tileWidth - (x * tileWidth), EEYLevel + (EEHEIGHT * tileWidth), settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                    tiles.add(new TileInfo(-settings.tileWidth - (x * tileWidth), EEYLevel, settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                }
                for (int x = 0; x <= EEHEIGHT ; x++) {
                    tiles.add(new TileInfo(-settings.tileWidth - (EEWIDTH * tileWidth), EEYLevel + (x * tileWidth), settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                    tiles.add(new TileInfo(settings.roomWidth + (EEWIDTH * tileWidth), EEYLevel + (x * tileWidth), settings.tileWidth, settings.tileWidth, TileType.PLATFORM));
                }
                i += EEHEIGHT;
            }
        }
    }

    private void createIslandStrip(int xStart, float yLevel) {
        platformTiles.add(new TileInfo(xStart, yLevel + islandHeight, tileWidth, tileWidth, TileType.PLATFORM)); //placing standeable platform tiles
        for (int y = 1; y < 3; y++) {
            dirtTiles.add(new TileInfo(xStart, (yLevel) - (tileWidth * y) + islandHeight, tileWidth, tileWidth, TileType.DIRT)); //placing blocks below main platform for aesthetics
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

            noiseValue += pg.noise(i * 0.02f); //Scale input to smoothen noise
            int yPosition = (int) ((noiseValue + 1) / 2 * ((settings.groundMax - (settings.groundMin)) - settings.groundMin) + settings.groundMin);
            yPos = ProcGen.fitGrid(yPosition, tileWidth);

//            if (lastTileY + tileWidth < yPos) { //if the tile is more than one tile spaces higher than the last tile
//                int x = 1;
//                while (lastTileY + (tileWidth * x) <= yPos - tileWidth) {
//                    dirtTiles.add(new TileInfo((i * tileWidth), lastTileY + (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
//                    x++;
//                }
//            } else if (lastTileY - tileWidth > yPos) { //if the tile is more than one tile spaces lower than the last tile
//                int x = 1;
//                while (lastTileY - (tileWidth * x) > yPos) {
//                    dirtTiles.add(new TileInfo((i * tileWidth - tileWidth), lastTileY - (tileWidth * x), tileWidth, tileWidth, TileType.DIRT));
//                    x++;
//                }
//            }
            fillGround(i);

            platformTiles.add(new TileInfo(tileWidth * i, yPos, tileWidth, tileWidth, TileType.PLATFORM));

            if (canCreateIsland) {
                int var = random.nextInt(15);
                if (var == 12) { //this is the only condition where an island will be created
                    islandGoalLength = random.nextInt(8, 15); // creating an island of a width between 5 and 15 (islands can be near continuous after each other so no point in making it too big
                    islandCurrentLength = 1;
                    islandCurrentY = yPos;
                    creatingIsland = true;
                    canCreateIsland = false;
                    islandHeight = random.nextInt(370,550);
                    islandHeight = pg.fitGrid(Math.round(islandHeight), tileWidth);
                }
            }
            else if (!creatingIsland) { //
                if (canCreateIslandCount >= 2) {
                    canCreateIsland = true;
                }
                else {
                    canCreateIslandCount++;
                }
            }
            if (creatingIsland) {
                //keep track of island max and current length
                if (islandCurrentLength <= islandGoalLength) {
                    if (yPos < islandCurrentY - tileWidth) {
                        islandCurrentY -= tileWidth;
                    }
                    else if (yPos > islandCurrentY + tileWidth) {
                        islandCurrentY += tileWidth;
                    }
                    createIslandStrip(i * tileWidth, islandCurrentY);
                    islandCurrentLength++;
                }
                else {
                    creatingIsland = false;
                }
            }
            EEYLevel = Math.round(yPos);
        }
    }

    private void fillGround(int i) {
        int j = 0;
        while (yPos - (tileWidth * j) >= settings.groundMin
        ) { //filling in the below tiles
            dirtTiles.add(new TileInfo(i * tileWidth, yPos - (tileWidth * j), tileWidth, tileWidth, TileType.DIRT));
            j++;
        }
        lastTileY = yPos;
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
