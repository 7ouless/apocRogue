package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

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
import io.github.apocRogue.map.GenerationSettings;
import io.github.apocRogue.map.MapManager;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.DirtTile;
import io.github.apocRogue.weapons.StatKeys;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponFactory;
import io.github.apocRogue.weapons.WeaponIDDecoder;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;

public class GameWorld {
    private final Stage stage;
    private PlayerActor player;
    private Skin skin;
    private Inventory inventory;
    private MapManager mapManager;
    private GenerationSettings generationSettings;

    // ID system managers
    private ItemManager itemManager;
    private WeaponTypeRegistry typeRegistry;

    // Actors & textures
    private Array<DummyActor> enemies = new Array<>();
    private Array<FlyingEnemyActor> flyingEnemies = new Array<>();
    private Array<MiniSamuraiActor> samuraiActorArray = new Array<>();
    private Array<ChestActor> chests = new Array<>();

    private Texture playerTexture, dummyTexture, chestTexture,
        flyingCreatureTexture, samuraiTexture;

    public GameWorld(Stage stage) {
        this.stage = stage;
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
        playerTexture         = new Texture("ui/sprite.png");
        dummyTexture          = new Texture("ui/dummy.png");
        chestTexture          = new Texture("ui/chest.png");
        samuraiTexture        = new Texture("ui/samurai.jpeg");
        flyingCreatureTexture = new Texture("ui/bat.png");

        // Player & Inventory
        player = new PlayerActor(playerTexture);
        float spawnY = mapManager.settings.groundMax + 10;
        player.setPosition(50, spawnY);
        stage.addActor(player);

        inventory = new Inventory(skin);
        player.setInventory(inventory);

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
                    Map<String,Integer> baseStats = itemManager.getBaseStats(typeID);
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
        // (zeroed out or adjust as you like)
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            DummyActor d = new DummyActor(dummyTexture,
                pos!=null?pos[0]:400,
                pos!=null?pos[1]:mapManager.settings.groundMax+10
            );
            enemies.add(d);
            stage.addActor(d);
        }


        // Spawn chests
        int chestCount = DifficultyLevelGen.getChestCount();
        for (int i = 0; i < chestCount; i++) {
            float[] pos = getRandomSpawnPosition();
            float x = pos!=null?pos[0]:500;
            float y = pos!=null?pos[1]:mapManager.settings.groundMax+10;

            // New ChestActor ctor
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
    }

    private float[] getRandomSpawnPosition() {
        Array<PlatformTile> plats = new Array<>();
        for (Actor a : stage.getActors()) {
            if (a instanceof PlatformTile) plats.add((PlatformTile)a);
        }
        if (plats.size == 0) return null;
        PlatformTile t = plats.random();
        return new float[]{ t.getX() + t.getWidth()/2f, t.getY() + t.getHeight() };
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

    public PlayerActor getPlayer() { return player; }
    public Inventory getInventory() { return inventory; }
    public Stage getStage()        { return stage;     }

    public void dispose() {
        stage.dispose();
        playerTexture.dispose();
        dummyTexture.dispose();
        chestTexture.dispose();
        samuraiTexture.dispose();
        flyingCreatureTexture.dispose();
    }
}
