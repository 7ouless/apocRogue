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
import java.util.Set;

import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.mobs.FlyingEnemyActor;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.globals.difficulty.CurrentDificulty;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;
import io.github.apocRogue.globals.ids.ClassDigit;
import io.github.apocRogue.globals.physics.SoundPhysics;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.map.*;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
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

    // ID system: only cosmetic registry
    private WeaponTypeRegistry typeRegistry;

    // Actors & textures
    private Array<WolfActor> enemies = new Array<>();
    private Array<FlyingEnemyActor> flyingEnemies = new Array<>();
    private Array<MiniSamuraiActor> samuraiActorArray = new Array<>();
    private Array<ChestActor> chests = new Array<>();

    private float spawnOffsetY = 22f;

    private Texture playerTexture, playerAttackTexture, wolfNormalTexture, wolfAttackTexture, chestTexture,
        flyingCreatureTexture, samuraiTexture;

    public GameWorld(Stage stage, boolean isFinalWorld) {
        this.stage = stage;
        this.isFinalWorld = isFinalWorld;
    }


    private final float baseSpawnX  = 30f;
    private final float extraSpawnX = 20f;

    private void spawnPlayer() {
        player = new PlayerActor(playerTexture, playerAttackTexture);
        float spawnX  = baseSpawnX + extraSpawnX;
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
        wolfNormalTexture    = new Texture("ui/wolf.png");
        wolfAttackTexture    = new Texture("ui/wolf-attack.png");
        chestTexture = new Texture("ui/chest.png");
        samuraiTexture = new Texture("ui/samurai.jpeg");
        flyingCreatureTexture = new Texture("ui/bat.png");

        // Player & Inventory
        inventory = new Inventory(skin);
        spawnPlayer();

        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json"); // name, textures, projectile flag

        Set<String> idSet = typeRegistry.getAllTypeIDs();
        Array<String> allTypeIDs = new Array<>(idSet.toArray(new String[0]));

        // Rehydrate saved weapons using new ID scheme
        List<String> savedNames = InventoryPreferences.load();
        for (String name : savedNames) {
            for (String localTypeID : allTypeIDs) {
                // 1) build the 3-char global prefix: '1' (weapon) + localTypeID (e.g. "01")
                    String globalID = ClassDigit.prefix(ClassDigit.WEAPON, localTypeID);
                // 2) lookup cosmetic info by that full ID
                 WeaponTypeInfo info = typeRegistry.getByGlobalID(globalID);
                if (info.getName().equals(name)) {
                    // 3) construct the dummy weapon with that same globalID
                        Weapon w = new Weapon(
                        globalID,
                        info.getName(),
                        0,
                        new Texture(Gdx.files.internal(info.getTexturePath())),
                        info.isProjectileType(),
                         0,
                         info.getAmmoTexture(),
                        0, 0,
                        0, 0, 0
                        );
                    inventory.addItem(w);
                     break;
                   }
                }
            }

        // Spawn enemies
        int enemyCount = DifficultyLevelGen.getEnemyCount();
        for (int i = 0; i < enemyCount; i++) {
            float spawnX = MathUtils.random(100, mapManager.settings.roomWidth - 100);
            float groundY = getGroundHeightAtX(spawnX);
            float spawnY = groundY + spawnOffsetY;  // Ensure correct vertical offset like player

            WolfActor wolf = new WolfActor( wolfNormalTexture, wolfAttackTexture, spawnX, spawnY);
            enemies.add(wolf);
            stage.addActor(wolf);
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
                typeRegistry
            );
            chests.add(chest);
            stage.addActor(chest);
        }

        doors.clear();
        spawnDoors();
    }

    private float[] getRandomSpawnPosition() {
        Array<Actor> spawnTiles = new Array<>();
       for (Actor a : stage.getActors()) {
                if (a instanceof FloorTile || a instanceof PlatformTile) {
                spawnTiles.add(a);
                }
            }
        if (spawnTiles.size == 0) return null;
       Actor t = spawnTiles.random();
       return new float[]{
            t.getX() + t.getWidth() * 0.5f,
            t.getY() + t.getHeight()
       };
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
        if (player != null) {
            float feetX = player.getX() + player.getWidth() * 0.5f;
            float feetY = player.getY();
            boolean fellOff = player.getY() < 0;
            boolean hitDirt = isOverlappingWithDirt(feetX, feetY);
            if (fellOff || hitDirt) {
                float respawnX  = baseSpawnX + extraSpawnX;
                float groundY   = getGroundHeightAtX(respawnX);
                float respawnY  = groundY + player.getHeight() + spawnOffsetY;

                player.setPosition(respawnX, respawnY);
                player.velocityX = 0;
                player.velocityY = 0;
                return;

            }
            }
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
        float borderMargin = 110f;
        float baseX = end.getX() + end.getWidth() * 0.5f - borderMargin;
        float baseY = end.getY() + end.getHeight();

        // separation in world‐units between the two doors
        float sep = 200f;

        if (!isFinalWorld) {
            // Worlds 1–4: three different continue doors
                for (int r = 1; r <= 3; r++) {
                float x = baseX + (r - 2) * sep;
                doors.add(new Door(Door.Type.CONTINUE,
                 new Vector2(x, baseY),
                 r));
                            }
            } else {
            // World 5: exactly one continue + one extract
                // Continue door – center‐left
            Vector2 contPos = new Vector2(baseX - sep * 0.5f, baseY);
            doors.add(new Door(Door.Type.CONTINUE,
                contPos,
                CurrentDificulty.getRadiation()));

            // Extract door – center‐right
            Vector2 exitPos = new Vector2(baseX + sep * 0.5f, baseY);
            doors.add(new Door(Door.Type.EXTRACT,
                exitPos));
            }

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
        wolfNormalTexture.dispose();
        wolfAttackTexture.dispose();
        chestTexture.dispose();
        samuraiTexture.dispose();
        flyingCreatureTexture.dispose();
    }
    }

