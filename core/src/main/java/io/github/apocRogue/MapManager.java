package io.github.apocRogue;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds either a procedural or predefined set of tiles (including the floor).
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
        // Load or define your layouts
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

    public void generateMap(Stage stage) {
        if (useProcedural) {
            List<TileInfo> tiles = proceduralGen.generateMap(settings);
            for (TileInfo info : tiles) {
                Actor tileActor = createTileActor(info);
                stage.addActor(tileActor);
            }
            // Also add a base floor
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

    // Example layouts
    private List<TileInfo> createLayout1() {
        List<TileInfo> layout = new ArrayList<>();
        layout.add(new TileInfo(0, 0, settings.levelWidth, 50, TileType.GROUND));
        layout.add(new TileInfo(200, 120, 100, 20, TileType.PLATFORM));
        layout.add(new TileInfo(400, 200, 120, 20, TileType.PLATFORM));
        layout.add(new TileInfo(600, 50, 30, 30, TileType.HAZARD));
        return layout;
    }

    private List<TileInfo> createLayout2() {
        List<TileInfo> layout = new ArrayList<>();
        layout.add(new TileInfo(0, 0, settings.levelWidth, 50, TileType.GROUND));
        layout.add(new TileInfo(300, 150, 150, 20, TileType.PLATFORM));
        layout.add(new TileInfo(700, 250, 120, 20, TileType.PLATFORM));
        layout.add(new TileInfo(500, 50, 40, 40, TileType.HAZARD));
        return layout;
    }
}
