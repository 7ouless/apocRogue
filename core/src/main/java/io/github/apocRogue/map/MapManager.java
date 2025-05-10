package io.github.apocRogue.map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MapManager {

    public static GenerationSettings settings = GenerationType.PLAINS.settings;

    private boolean canCreateIsland = false;
    private boolean creatingIsland = false;
    private int canCreateIslandCount = 0;
    private int islandGoalLength;
    private int islandCurrentLength;
    private float islandCurrentY;

    //grass shit
    private final boolean grassUseEdgeSize     = false;    // if true, grass dims are based on edge
    private final float   grassWidthMultiplier  =0.10f;
    private final float   grassHeightMultiplier = 0.8f;

    private int tileWidth = settings.tileWidth;
    private int tileHeight = settings.tileHeight;

    private float lastTileY = 0; //!!!when created should be assigned to the entrance's floor height!!!//

    private float islandYPos = 600;
    private float yPos = 300;

    private final float EDGE_X_OFFSET = settings.tileWidth * 0.05f; //Jimmy is going to kill me

    private boolean platform = false;
    private final float minPlatformY = settings.tileHeight * 5;    //start 5 tiles up
    private final float maxPlatformY = settings.roomHeight - 100;  //100px below ceiling
    private float currentIslandPlatY;

    private static final boolean GRASS_ENABLED = true;

    private int octaves = settings.octaves;  //#of octaves

    private List<TileInfo> platformTiles = new ArrayList<>();
    private List<TileInfo> dirtTiles = new ArrayList<>();
    private List<TileInfo> borderTiles = new ArrayList<>();
    private List<TileInfo> edgeTiles = new ArrayList<>();
    private List<TileInfo> grassTiles = new ArrayList<>();
    private List<TileInfo> treeTiles = new ArrayList<>();

    private final ProcGen pg = new ProcGen();
    private final Random random = new Random();

    public void generateMap(Stage stage) {
        createRoom();

        for (TileInfo info : treeTiles) {
            stage.addActor(createTileActor(info));
        }

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

        for (TileInfo info : grassTiles) {
            stage.addActor(createTileActor(info));
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

            i++;
        }
        i = 0;
        while (i <= settings.roomHeight/ settings.tileHeight) { //vertical tiles
            tiles.add(new TileInfo(-tileHeight, i*settings.tileHeight, tileHeight, tileHeight, TileType.BORDER));
            tiles.add(new TileInfo(settings.roomWidth, i* settings.tileHeight, tileHeight, tileHeight, TileType.BORDER));
            i++;
        }
    }

    private void createIslandStrip(int xStart, float baseY) {

        if (islandCurrentLength == 1) {
            float centerY = baseY + 600;
            float jitter  = tileHeight * 6;  // ±1 tile
            float rawY    = centerY + (random.nextFloat()*2*jitter - jitter);
            currentIslandPlatY = Math.max(minPlatformY,
                Math.min(maxPlatformY, rawY));
        }

        platformTiles.add(new TileInfo(
            xStart,
            currentIslandPlatY,
            tileWidth,
            tileHeight,
            TileType.PLATFORM
        ));
    }


    private void createGround() { // used to join the long platforms using procedurally generated terrain
        int seed = random.nextInt(99999999);
        pg.generatePermutationTable(seed);
        int tilesSinceLastTree = settings.treeGap;

        for (int i = 0; i < settings.roomWidth / tileWidth; i++) {
            //Per-column noise
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

            //Snap to grid
            int yRaw = (int)((noiseValue + 1f) / 2f
                * ((settings.groundMax / 2 - settings.groundMin) - settings.groundMin)
                + settings.groundMin);
            float unclampedY = ProcGen.fitGrid(yRaw, tileHeight);

            //Clamp steep jumps to ±1 tile
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
                yPos = unclampedY;
            }

            // Height-step check
            boolean heightChanged = (i > 0)
                && Math.abs(yPos - lastTileY) == tileHeight;

            //Draw the floor tile (20% taller)
            float floorW = tileWidth;
            float floorH = tileHeight * 1.2f;
            float floorY = yPos + tileHeight;

            platformTiles.add(new TileInfo(
                i * tileWidth,
                floorY,
                tileWidth,
                tileHeight,
                TileType.GROUND
            ));
            if (GRASS_ENABLED) {
                grassTiles.add(new TileInfo(
                    i * tileWidth,
                    floorY + tileHeight * 0.4f,
                    tileWidth,
                    tileHeight,
                    TileType.GRASS
                ));
            }

            // Tree Spawning
            if (tilesSinceLastTree >= settings.treeGap
                && random.nextFloat() < settings.treeDensity) {
                float w = settings.treeWidth;
                float h = settings.treeHeight;
                float x = i * tileWidth
                    + random.nextFloat() * (tileWidth - w);
                float y = floorY + tileHeight - settings.treeYOffset;
                boolean useAlt = random.nextBoolean();
                boolean flip   = random.nextBoolean();

                treeTiles.add(new TileInfo(
                    x, y, w, h,
                    TileType.TREE,
                    flip,
                    useAlt,
                    ""
                ));
                // reset counter after spawning:
                tilesSinceLastTree = 0;
            } else {
                // increment if we didn’t spawn one here:
                tilesSinceLastTree++;
            }


            //Place the edge-square on the lower tile
            if (heightChanged) {
                // determine which tile is lower
                boolean rising = yPos > lastTileY;
                int lowerIdx = rising ? (i - 1) : i;
                float lowerY = Math.min(yPos, lastTileY);
                float lowerX = lowerIdx * tileWidth;

                boolean flipX = rising;
                boolean useAltTexture = random.nextBoolean();

                float edgeHeight = tileHeight *0.3f;      // match floor tile's visual height
                float edgeWidth = tileWidth * 0.1f;        // shrink width to 50%

                // compute a “base” X exactly as before, then apply your offset:
                float baseX = rising
                    ? lowerX + floorW - edgeWidth
                    : lowerX;
                float squareX = baseX + (rising ? +EDGE_X_OFFSET : -EDGE_X_OFFSET);
                float squareY = lowerY + tileHeight * 2;

                edgeTiles.add(new TileInfo(
                    squareX,
                    squareY,
                    edgeWidth,
                    edgeHeight,
                    TileType.EDGE,
                    flipX,
                    useAltTexture,
                    ""
                ));


                String edgeType = useAltTexture ? "edge2" : "edge1";
                if (GRASS_ENABLED) {
                    float grassXOffset = tileWidth * -0.08f;
                    float grassYOffset = tileHeight * 0.5f;

                    float grassW = grassUseEdgeSize
                        ? edgeWidth  * grassWidthMultiplier
                        : tileWidth  * grassWidthMultiplier;
                    float grassH = grassUseEdgeSize
                        ? edgeHeight * grassHeightMultiplier
                        : tileHeight * grassHeightMultiplier;

                    // anchor at the “inner” side of the edge, then nudge toward the tall side
                    float grassBaseX = flipX
                        ? (squareX + edgeWidth)
                        : squareX;
                    float grassX = grassBaseX - grassW/2f
                                         + (flipX ? +grassXOffset : -grassXOffset);

                    grassTiles.add(new TileInfo(
                        grassX,
                        squareY + grassYOffset,
                        grassW,
                        grassH,
                        TileType.GRASS,
                        flipX,
                        false,
                        edgeType
                    ));
                }


            }

            //Fill in the dirt beneath this column
            fillGround(i, yPos);

            //Update lastTileY for next iteration
            lastTileY = yPos;

            //Island-creation logic
            if (canCreateIsland) {
                int var = random.nextInt(15);
                if (var > 4) {
                    islandGoalLength    = random.nextInt(1, 4);
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



    private void fillGround(int i, float topY) {
        // 1) Fill normally down to groundMin
        int j = 0;
        while (topY - (tileHeight * j) >= settings.groundMin) {
            dirtTiles.add(new TileInfo(
                i * tileWidth,
                topY - (tileHeight * j),
                tileWidth,
                tileHeight,
                TileType.DIRT
            ));
            j++;
        }

        // 2) Add 5 extra dirt layers below that
        for (int extra = 1; extra <= 5; extra++) {
            dirtTiles.add(new TileInfo(
                i * tileWidth,
                // continue stacking downwards
                topY - (tileHeight * (j + extra - 1)),
                tileWidth,
                tileHeight,
                TileType.DIRT
            ));
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
                return new EdgeTile(info.x, info.y, info.width, info.height, info.flipX, info.useAltTexture);
            case GRASS:
                return new GrassOverlayTile(info.x, info.y, info.width, info.height, info.flipX, info.grassType);
            case TREE:    return new TreeTile(info.x, info.y, info.width, info.height,info.flipX, info.useAltTexture);

            default: // PLATFORM
                return new PlatformTile(info.x, info.y, info.width, info.height);
        }
    }
}
