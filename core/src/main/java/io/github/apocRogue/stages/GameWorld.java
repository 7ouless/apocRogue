package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.mobs.DummyActor;
import io.github.apocRogue.actors.mobs.FlyingEnemyActor;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;
import io.github.apocRogue.globals.physics.SoundPhysics;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.inventory.general.ItemManager;
import io.github.apocRogue.map.*;
import io.github.apocRogue.weapons.StatKeys;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponFactory;
import io.github.apocRogue.weapons.WeaponIDDecoder;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;
import io.github.apocRogue.actors.mapEntities.Door;
import io.github.apocRogue.map.MapManager;


public class GameWorld {
    private final Stage stage;
    private PlayerActor player;
    private Skin skin;
    private Inventory inventory;
    private MapManager mapManager;
    private GenerationSettings generationSettings;

    private final boolean isFinalWorld;
    private final List<Door> doors = new ArrayList<>();

    // ID system managers
    private ItemManager itemManager;
    private WeaponTypeRegistry typeRegistry;

    // Actors & textures
    private Array<DummyActor> enemies = new Array<>();
    private Array<FlyingEnemyActor> flyingEnemies = new Array<>();
    private Array<MiniSamuraiActor> samuraiActorArray = new Array<>();
    private Array<ChestActor> chests = new Array<>();

    private float spawnOffsetY = 22f;

    private Texture playerTexture, playerAttackTexture, dummyTexture, chestTexture,
        flyingCreatureTexture, samuraiTexture;

    public GameWorld(Stage stage, boolean isFinalWorld) {
        this.stage = stage;
        this.isFinalWorld = isFinalWorld;
    }


    private void spawnPlayer() {
        player = new PlayerActor(playerTexture, playerAttackTexture);
        float spawnX  = 30f;
        float groundY = getGroundHeightAtX(spawnX);
        float spawnY  = groundY + player.getHeight() + spawnOffsetY;
        player.setPosition(spawnX, spawnY);
        player.setInventory(inventory);
        stage.addActor(player);
    }

    public void initialize() {
        // Map & UI setup
        generationSettings = new GenerationSettings();
        generationSettings.roomWidth = 3000;
        generationSettings.platformDensity = 5;

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        mapManager = new MapManager();
        mapManager.generateMap(stage);

        //Load textures
        playerTexture = new Texture("ui/main-character.png");
        playerAttackTexture = new Texture("ui/main-character-attack.png");
        dummyTexture = new Texture("ui/dummy.png");
        chestTexture = new Texture("ui/chest.png");
        samuraiTexture = new Texture("ui/samurai.jpeg");
        flyingCreatureTexture = new Texture("ui/bat.png");

        // Player & Inventory
        inventory = new Inventory(skin);
        spawnPlayer();

        //ID system: load base‐stat table and static metadata
        itemManager = new ItemManager();
        itemManager.loadBaseData("ui/items.json");            // base stats + typeID

        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");            // name, textures, projectile flag

        Array<String> allTypeIDs = itemManager.getAllTypeIDs();

        //Rehydrate saved weapons (stash)
        List<String> savedNames = InventoryPreferences.load();
        for (String name : savedNames) {
            for (String typeID : allTypeIDs) {
                WeaponTypeInfo info = typeRegistry.get(typeID);
                if (info.getName().equals(name)) {
                    // roll at diff=1,1 so you get base stats
                    Map<String, Integer> baseStats = itemManager.getBaseStats(typeID);
                    String id = WeaponFactory.rollAndEncode(typeID, baseStats, 1, 1);
                    WeaponIDDecoder.Decoded d = WeaponIDDecoder.decode(id);

                    // build a fully‐decoded weapon
                    Weapon w = new Weapon(
                        id,
                        info.getName(),
                        d.stats.get("damage"),
                        new Texture(Gdx.files.internal(info.getTexturePath())),
                        info.isProjectileType(),
                        d.stats.get("projectileValue"),
                        info.getAmmoTexture(),
                        d.stats.get("animationSpeed"),
                        d.stats.get("noiseLevel"),
                        d.stats.get("dashSpeed"),
                        d.stats.get("dashDuration"),
                        d.stats.get("dashCooldown")
                    );
                    inventory.addItem(w);
                    break;
                }
            }
        }

        // Spawn enemies
        int enemyCount = DifficultyLevelGen.getEnemyCount();
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            DummyActor d = new DummyActor(dummyTexture,
                pos != null ? pos[0] : 400,
                pos != null ? pos[1] : mapManager.settings.groundMax + 10
            );
            enemies.add(d);
            stage.addActor(d);
        }


        // Spawn chests
        int chestCount = DifficultyLevelGen.getChestCount();
        for (int i = 0; i < chestCount; i++) {
            float[] pos = getRandomSpawnPosition();
            float x = pos != null ? pos[0] : 500;
            float y = pos != null ? pos[1] : mapManager.settings.groundMax + 10;


            ChestActor chest = new ChestActor(
                chestTexture,
                x, y,
                allTypeIDs,
                skin,
                itemManager,
                typeRegistry
            );
            chests.add(chest);
            stage.addActor(chest);
        }

        doors.clear();
        spawnDoors();
    }

    private float[] getRandomSpawnPosition() {
        Array<PlatformTile> plats = new Array<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof PlatformTile) plats.add((PlatformTile) a);
        }
        if (plats.size == 0) return null;
        PlatformTile t = plats.random();
        return new float[]{t.getX() + t.getWidth() / 2f, t.getY() + t.getHeight()};
    }

    private boolean isOverlappingWithDirt(float x, float y) {
        for (Actor a : stage.getActors()) {
            if (a instanceof DirtTile) {
                float dx = a.getX(), dy = a.getY();
                if (x >= dx && x <= dx + a.getWidth()
                    && y >= dy && y <= dy + a.getHeight())
                    return true;
            }
        }
        return false;
    }

    public void update(float delta) {
        stage.act(delta);
        SoundPhysics.updateDebugEvents(delta);
        }


    private float getGroundHeightAtX(float x) {
        float maxY = 0;
        for (Actor a : stage.getActors()) {
            if (a instanceof PlatformTile
                || a instanceof FloorTile
                || a instanceof BorderTile) {
                float tileX = a.getX();
                float tileW = a.getWidth();
                if (x >= tileX && x <= tileX + tileW) {
                    float topY = a.getY() + a.getHeight();
                    if (topY > maxY) maxY = topY;
                }
            }
        }
        return maxY;
    }

    public PlayerActor getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Stage getStage() {
        return stage;
    }

    private Vector2 findExitPosition() {
        // back off one tile so the door sits fully inside the room
        float tileW = MapManager.settings.tileWidth;
        float x = MapManager.settings.roomWidth - tileW * 1.5f;
        // snap to ground height at that X
        float y = getGroundHeightAtX(x);
        return new Vector2(x, y);
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

