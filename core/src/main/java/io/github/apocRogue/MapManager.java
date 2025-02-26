package io.github.apocRogue;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether to use a procedural or predefined layout, then builds it on the Stage.
 */
public class MapManager {
    private boolean useProcedural = true;
    private ProceduralGenerator proceduralGen;
    private List<List<TileInfo>> predefinedLayouts;
    private GenerationSettings settings;
    private int currentLayoutIndex = 0;

    public MapManager(GenerationSettings settings) {
        this.settings = settings;
        this.proceduralGen = new ProceduralGenerator();
        // Initialize or load predefined layouts
        predefinedLayouts = new ArrayList<>();
        predefinedLayouts.add(createLayout1());
        predefinedLayouts.add(createLayout2());
    }

    public void setUseProcedural(boolean useProcedural) {
        this.useProcedural = useProcedural;
    }

    public void setCurrentLayoutIndex(int index) {
        this.currentLayoutIndex = Math.min(index, predefinedLayouts.size() - 1);
        this.useProcedural = false;
    }

    /**
     * Generate the map according to the current mode (procedural or predefined)
     * and add all tile actors to the given stage.
     */
    public void generateMap(Stage stage) {
        if (useProcedural) {
            List<TileInfo> tiles = proceduralGen.generateMap(settings);
            for (TileInfo info : tiles) {
                Actor tileActor = createTileActor(info);
                stage.addActor(tileActor);
            }
            // Also add a base ground
            stage.addActor(new FloorTile(0, 0, settings.levelWidth, 50));
        } else {
            // Use a predefined layout
            List<TileInfo> layout = predefinedLayouts.get(currentLayoutIndex);
            for (TileInfo info : layout) {
                Actor tileActor = createTileActor(info);
                stage.addActor(tileActor);
            }
        }
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

    // Example predefined layouts
    private List<TileInfo> createLayout1() {
        List<TileInfo> layout = new ArrayList<>();
        // Base ground
        layout.add(new TileInfo(0, 0, settings.levelWidth, 50, TileType.GROUND));
        // Some platforms
        layout.add(new TileInfo(200, 120, 100, 20, TileType.PLATFORM));
        layout.add(new TileInfo(400, 200, 120, 20, TileType.PLATFORM));
        // A hazard
        layout.add(new TileInfo(600, 50, 30, 30, TileType.HAZARD));
        return layout;
    }

    private List<TileInfo> createLayout2() {
        List<TileInfo> layout = new ArrayList<>();
        // Another ground
        layout.add(new TileInfo(0, 0, settings.levelWidth, 50, TileType.GROUND));
        // Platforms, hazards, etc.
        layout.add(new TileInfo(300, 150, 150, 20, TileType.PLATFORM));
        layout.add(new TileInfo(700, 250, 120, 20, TileType.PLATFORM));
        layout.add(new TileInfo(500, 50, 40, 40, TileType.HAZARD));
        return layout;
    }
}
