package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.mapEntities.Door;
import io.github.apocRogue.actors.mobs.DummyActor;
import io.github.apocRogue.actors.mobs.FlyingEnemyActor;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;
import io.github.apocRogue.globals.physics.SoundPhysics;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;
import io.github.apocRogue.inventory.general.ItemManager;
import io.github.apocRogue.weapons.StatKeys;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.map.*;

/**
 * World setup including player, map, enemies, chests, and inventory.
 */
public class GameWorld {
    private final Stage stage;
    private PlayerActor player;
    private Skin skin;
    private Inventory inventory;
    private MapManager mapManager;
    private GenerationSettings generationSettings;
    private final boolean isFinalWorld;
    private final List<Door> doors = new ArrayList<>();

    // Managers
    private ItemManager itemManager;
    private WeaponTypeRegistry typeRegistry;
    private DBManager dbManager;

    // Actors & textures
    private Texture playerTexture, playerAttackTexture;
    private Texture dummyTexture, chestTexture, flyingCreatureTexture, samuraiTexture;

    public GameWorld(Stage stage, boolean isFinalWorld) {
        this.stage = stage;
        this.isFinalWorld = isFinalWorld;
    }


    public void initialize() {
        // Load map and UI skin
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        generationSettings = new GenerationSettings();
        generationSettings.roomWidth = 3000;
        generationSettings.platformDensity = 5;
        mapManager = new MapManager();
        mapManager.generateMap(stage);

        // Load textures
        playerTexture        = new Texture("ui/main-character.png");
        playerAttackTexture  = new Texture("ui/main-character-attack.png");
        dummyTexture         = new Texture("ui/dummy.png");
        chestTexture         = new Texture("ui/chest.png");
        samuraiTexture       = new Texture("ui/samurai.jpeg");
        flyingCreatureTexture = new Texture("ui/bat.png");

        // Initialize inventory and player
        inventory = new Inventory(skin);
        spawnPlayer();

        // Load item base data and weapon metadata
        itemManager = new ItemManager();
        itemManager.loadBaseData("ui/items.json");

        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");

        // Prepare possible types for chests
        Array<String> possibleTypeIDs = itemManager.getAllTypeIDs();

        // Fetch persisted inventory from backend
        dbManager = DBManager.get();
        dbManager.fetchInventory(new JsonCallback() {
            @Override
            public void onSuccess(String json) {

            }

            @Override
            public void onSuccess(JsonValue dataArray) {
                for (JsonValue item : dataArray) {
                    String code     = item.getString("itemCode");
                    String typeID   = item.getString("typeID");
                    JsonValue statsJ = item.get("stats");

                    Map<String, Integer> stats = new LinkedHashMap<>();
                    for (String key : StatKeys.ALL) {
                        stats.put(key, statsJ.getInt(key, 0));
                    }

                    WeaponTypeInfo info = typeRegistry.get(typeID);
                    Texture weapTex = new Texture(Gdx.files.internal(info.getTexturePath()));
                    Texture ammoTex = new Texture(Gdx.files.internal(info.getAmmoTexture()));

                    Weapon w = new Weapon(code, info, stats, weapTex, ammoTex);
                    inventory.addItem(w);
                }
            }

            @Override
            public void onFailure(String error) {
                Gdx.app.error("GameWorld", "Inventory load failed: " + error);
            }

            @Override public void onError(Throwable t) { /* handle network errors */ }
        });

        // Spawn enemies
        int enemyCount = DifficultyLevelGen.getEnemyCount();
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            float x = pos != null ? pos[0] : 400f;
            float y = pos != null ? pos[1] : MapManager.settings.groundMax + 10f;
            DummyActor d = new DummyActor(dummyTexture, x, y);
            stage.addActor(d);
        }

        // Spawn chests
        int chestCount = DifficultyLevelGen.getChestCount();
        for (int i = 0; i < chestCount; i++) {
            float[] pos = getRandomSpawnPosition();
            float x = pos != null ? pos[0] : 500f;
            float y = pos != null ? pos[1] : MapManager.settings.groundMax + 10f;

            ChestActor chest = new ChestActor(
                chestTexture,
                x, y,
                possibleTypeIDs,
                skin,
                itemManager,
                typeRegistry
            );
            stage.addActor(chest);
        }

        spawnDoors();
    }

    private void spawnPlayer() {
        player = new PlayerActor(playerTexture, playerAttackTexture);
        float spawnX = 30f;
        float spawnY = getGroundHeightAtX(spawnX) + player.getHeight() + 22f;
        player.setPosition(spawnX, spawnY);
        player.setInventory(inventory);
        stage.addActor(player);
    }

    private float getGroundHeightAtX(float x) {
        float maxY = 0;
        for (Actor a : stage.getActors()) {
            if (a instanceof io.github.apocRogue.map.PlatformTile
                || a instanceof io.github.apocRogue.map.FloorTile
                || a instanceof io.github.apocRogue.map.BorderTile) {
                float ax = a.getX(), aw = a.getWidth();
                if (x >= ax && x <= ax + aw) {
                    maxY = Math.max(maxY, a.getY() + a.getHeight());
                }
            }
        }
        return maxY;
    }

    private float[] getRandomSpawnPosition() {
        Array<io.github.apocRogue.map.PlatformTile> plats = new Array<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof io.github.apocRogue.map.PlatformTile) plats.add((io.github.apocRogue.map.PlatformTile)a);
        }
        return plats.size > 0 ? new float[]{plats.random().getX(), plats.random().getY()} : null;
    }


    private void spawnDoors() {
        doors.clear();
        // collect every FloorTile
        List<FloorTile> floors = new ArrayList<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof FloorTile) floors.add((FloorTile)a);
        }
        if (floors.isEmpty()) return;

        // pick the rightmost floor
        floors.sort((f1, f2) -> Float.compare(f1.getX(), f2.getX()));
        FloorTile end = floors.get(floors.size() - 1);

        // base world‐position: center atop that tile
        float borderMargin = 80f;
        float baseX = end.getX() + end.getWidth() * 0.5f - borderMargin;
        float baseY = end.getY() + end.getHeight();

        // separation in world‐units between the two doors
        float sep = 200f;

        // CONTINUE door always at the end
        Vector2 contPos = new Vector2(baseX - sep * 0.5f, baseY);
        doors.add(new Door(Door.Type.CONTINUE, contPos));

        // EXTRACT only on final world, offset the other direction
        if (isFinalWorld) {
            Vector2 exitPos = new Vector2(baseX + sep * 0.5f, baseY);
            doors.add(new Door(Door.Type.EXTRACT, exitPos));
        }

        // add them to the stage
        doors.forEach(stage::addActor);
    }




    public Door getOverlappingDoor() {
        for (Door d : doors) {
            if (d.getBounds().overlaps(player.getBounds())) {
                return d;
            }
        }
        return null;
    }

    public void update(float delta) {
        stage.act(delta);
        SoundPhysics.updateDebugEvents(delta);
    }
    public Inventory getInventory() {
        return inventory;
    }
    public PlayerActor getPlayer() {
        return player;
    }
    public void dispose() {
        stage.clear();
        playerTexture.dispose();
        playerAttackTexture.dispose();
        dummyTexture.dispose();
        chestTexture.dispose();
        samuraiTexture.dispose();
        flyingCreatureTexture.dispose();
    }
}
