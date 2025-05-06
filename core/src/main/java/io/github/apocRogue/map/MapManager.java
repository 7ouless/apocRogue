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

    public static GenerationSettings settings = GenerationType.PLAINS.settings;

    private boolean canCreateIsland = false;
    private boolean creatingIsland = false;
    private int canCreateIslandCount = 0;
    private int islandGoalLength;
    private int islandCurrentLength;
    private float islandCurrentY;

    private int tileWidth = settings.tileWidth;
    private int tileHeight = settings.tileHeight;

    private float lastTileY = 0; //!!!when created should be assigned to the entrance's floor height!!!//

    private float islandYPos = 600;
    private float yPos = 300;

    private boolean platform = false;

    private int octaves = settings.octaves;  //#of octaves

    private List<TileInfo> platformTiles = new ArrayList<>();
    private List<TileInfo> dirtTiles = new ArrayList<>();
    private List<TileInfo> borderTiles = new ArrayList<>();
    private List<TileInfo> edgeTiles = new ArrayList<>();

    private final ProcGen pg = new ProcGen();
    private final Random random = new Random();

    public void generateMap(Stage stage) {
        createRoom();

        for (TileInfo info : dirtTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }
        for (TileInfo info : borderTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }
        for (TileInfo info : edgeTiles) {
            stage.addActor(createTileActor(info));
        }

        for (TileInfo info : platformTiles) {
            Actor tileActor = createTileActor(info);
            stage.addActor(tileActor);
        }
    }

    private void createRoom() {
        createBorders(borderTiles);
        createGround();
    }

    private void createBorders(List<TileInfo> tiles) {
        int i = 0;
        while (i < settings.roomWidth/settings.tileWidth) { //horizontal tiles
            tiles.add(new TileInfo(i*settings.tileWidth, settings.roomHeight, tileHeight, tileHeight, TileType.BORDER));
            tiles.add(new TileInfo(i*settings.tileWidth, 0, tileWidth, tileHeight, TileType.BORDER));
            i++;
        }
        i = 0;
        while (i <= settings.roomHeight/ settings.tileHeight) { //vertical tiles
            tiles.add(new TileInfo(-tileHeight, i*settings.tileHeight, tileHeight, tileHeight, TileType.BORDER));
            tiles.add(new TileInfo(settings.roomWidth, i* settings.tileHeight, tileHeight, tileHeight, TileType.BORDER));
            i++;
        }
    }

    private void createIslandStrip(int xStart, float yLevel) {
        platformTiles.add(new TileInfo(xStart, yLevel + 500, tileWidth, tileHeight, TileType.PLATFORM)); //placing standeable platform
        for (int y = 1; y < 3; y++) {
            dirtTiles.add(new TileInfo(xStart, (yLevel) - (tileHeight * y) + 500, tileWidth, tileHeight, TileType.DIRT)); //placing blocks below main platform for aesthetics
        }
    }

    private void createGround() { // used to join the long platforms using procedurally generated terrain
        int seed = random.nextInt(99999999);
        pg.generatePermutationTable(seed);

        for (int i = 0; i < settings.roomWidth / tileWidth; i++) {
            // ─────────── 1) Per-column noise ───────────
            float persistence = 0.5f;
            float frequency   = 0.5f;
            float amplitude   = 0.9f;
            float noiseValue  = 0f;

            for (int octave = 0; octave < octaves; octave++) {
                noiseValue += pg.noise(i * frequency) * amplitude;
                amplitude *= persistence;
                frequency *= 1.15f;
            }
            noiseValue += pg.noise(i * 0.2f);

            // ─────────── 2) Snap to grid ───────────
            int yRaw = (int)((noiseValue + 1f) / 2f
                * ((settings.groundMax / 2 - settings.groundMin) - settings.groundMin)
                + settings.groundMin);
            float unclampedY = ProcGen.fitGrid(yRaw, tileHeight);

            // ─────────── 3) Clamp steep jumps to ±1 tile ───────────
            if (i > 0) {
                float delta = unclampedY - lastTileY;
                if (delta > tileHeight) {
                    yPos = lastTileY + tileHeight;
                } else if (delta < -tileHeight) {
                    yPos = lastTileY - tileHeight;
                } else {
                    yPos = unclampedY;
                }
            } else {
                // first column just use computed value
                yPos = unclampedY;
            }

            // ─────────── 4) Height-step check ───────────
            boolean heightChanged = (i > 0)
                && Math.abs(yPos - lastTileY) == tileHeight;

            // ─────────── 5) Draw the floor tile (20% taller) ───────────
            float floorW = tileWidth;
            float floorH = tileHeight * 1.2f;
            platformTiles.add(new TileInfo(
                i * tileWidth,
                yPos,
                floorW,
                floorH,
                TileType.PLATFORM
            ));

            // ─────────── 6) Place the edge-square on the lower tile ───────────
            if (heightChanged) {
                // determine which tile is lower
                boolean rising = yPos > lastTileY;
                int lowerIdx = rising ? (i - 1) : i;
                float lowerY = Math.min(yPos, lastTileY);
                float lowerX = lowerIdx * tileWidth;

                float edgeSize = floorH * 0.835f;  // square side == floor height
                float squareX = rising
                    // step up: flush right of lower tile
                    ? lowerX + floorW - edgeSize
                    // step down: flush left of lower tile
                    : lowerX;
                float squareY = lowerY + floorH; // sits on top of lower floor

                edgeTiles.add(new TileInfo(
                    squareX,
                    squareY,
                    edgeSize,
                    edgeSize,
                    TileType.EDGE
                ));
            }

            // ─────────── 7) Fill in the dirt beneath this column ───────────
            fillGround(i);

            // ─────────── 8) Update lastTileY for next iteration ───────────
            lastTileY = yPos;

            // ─────────── 9) Island-creation logic (unchanged) ───────────
            if (canCreateIsland) {
                int var = random.nextInt(15);
                if (var > 12) {
                    islandGoalLength    = random.nextInt(8, 15);
                    islandCurrentLength = 1;
                    islandCurrentY      = yPos;
                    creatingIsland      = true;
                    canCreateIsland     = false;
                }
            } else if (!creatingIsland) {
                if (canCreateIslandCount >= 2) {
                    canCreateIsland = true;
                } else {
                    canCreateIslandCount++;
                }
            }
            if (creatingIsland) {
                if (islandCurrentLength <= islandGoalLength) {
                    if (yPos < islandCurrentY - tileWidth) {
                        islandCurrentY -= tileWidth;
                    } else if (yPos > islandCurrentY + tileWidth) {
                        islandCurrentY += tileWidth;
                    }
                    createIslandStrip(i * tileWidth, islandCurrentY);
                    islandCurrentLength++;
                } else {
                    creatingIsland = false;
                }
            }
        }
    }



    private void fillGround(int i) {
        int j = 0;
        while (yPos - (tileHeight * j) >= settings.groundMin
        ) { //filling in the below tiles
            dirtTiles.add(new TileInfo(i * tileWidth, yPos - (tileHeight * j), tileWidth, tileHeight, TileType.DIRT));
            j++;
        }
    }

    private Actor createTileActor(TileInfo info) {
        switch (info.type) {
            case GROUND:
                return new FloorTile(info.x, info.y, info.width, info.height);
            case HAZARD:
                return new HazardTile(info.x, info.y, info.width, info.height);
            case DIRT:
                return new DirtTile(info.x, info.y, info.width, info.height);
            case BORDER:
                return new BorderTile(info.x, info.y, info.width, info.height);
            case EDGE:
                return new EdgeTile(info.x, info.y, info.width);

            default: // PLATFORM
                return new PlatformTile(info.x, info.y, info.width, info.height);
        }
    }
}
