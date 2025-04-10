package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mobs.FlyingEnemyActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.mobs.DummyActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.difficulty.DifficultyLevelGen;
import io.github.apocRogue.inventory.Inventory;
import io.github.apocRogue.inventory.ItemManager;
import io.github.apocRogue.map.GenerationSettings;
import io.github.apocRogue.map.MapManager;
import io.github.apocRogue.map.DirtTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.weapons.Weapon;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.globals.physics.SoundPhysics;

public class GameWorld {

    private Stage stage;
    private PlayerActor player;
    private Skin skin;
    private Inventory inventory;
    private MapManager mapManager;
    private ItemManager itemManager;
    private GenerationSettings generationSettings;

    // Any other fields you need (enemy lists, chest lists, etc.)
    private Array<DummyActor> enemies;
    private Array<FlyingEnemyActor> flyingEnemies;
    private Array<ChestActor> chests;

    // Textures (you can store them here or load them externally)
    private Texture playerTexture;
    private Texture dummyTexture;
    private Texture chestTexture;
    private Texture flyingCreatureTexture;

    public GameWorld(Stage stage) {
        // We receive the stage from outside so that GameScreen still “owns”
        // the actual rendering environment, but the logic class can manipulate it
        this.stage = stage;

        // Initialize any data structures
        enemies = new Array<>();
        flyingEnemies = new Array<>();
        chests = new Array<>();
    }

    public void initialize() {
        // Set up generation settings
        generationSettings = new GenerationSettings();
        generationSettings.roomWidth = 3000;
        generationSettings.platformDensity = 5;
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create your map manager and generate the map
        mapManager = new MapManager();
        mapManager.generateMap(stage); // same logic as before

        // Create / load textures
        playerTexture = new Texture("ui/sprite.png");
        dummyTexture = new Texture("ui/dummy.png");
        chestTexture = new Texture("ui/chest.png");
        flyingCreatureTexture = new Texture("ui/bat.png");

        // Create the player
        player = new PlayerActor(playerTexture);
        // Start the player at some position, e.g., near the “groundMax” or anywhere else
        float spawnY = mapManager.settings.groundMax + 10;
        player.setPosition(50, spawnY);
        stage.addActor(player);

        // Initialize the inventory and attach it to the player
        inventory = new Inventory(skin);
        player.setInventory(inventory);

        // Load items/weapons for the chests
        itemManager = new ItemManager();
        itemManager.loadFromFile("items.json");
        Array<Weapon> allWeapons = itemManager.getLoadedWeapons();

        // Spawn enemies
        int enemyCount = DifficultyLevelGen.getEnemyCount();
        enemyCount = 1;
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            if (pos != null) {
                DummyActor dummy = new DummyActor(dummyTexture, pos[0], pos[1]);
                enemies.add(dummy);
                stage.addActor(dummy);
            } else {
                // fallback position
                DummyActor dummy = new DummyActor(dummyTexture, 400, mapManager.settings.groundMax + 10);
                enemies.add(dummy);
                stage.addActor(dummy);
            }
        }
        enemyCount = 0;
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            if (pos != null) {
                FlyingEnemyActor bat = new FlyingEnemyActor(flyingCreatureTexture, pos[0], pos[1]);
                flyingEnemies.add(bat);
                stage.addActor(bat);
            } else {
                // fallback position
                FlyingEnemyActor bat = new FlyingEnemyActor(flyingCreatureTexture, 400, mapManager.settings.groundMax + 10);
                flyingEnemies.add(bat);
                stage.addActor(bat);
            }
        }

        // Spawn chests
        int chestCount = DifficultyLevelGen.getChestCount();
        for (int i = 0; i < chestCount; i++) {
            float[] pos = getRandomSpawnPosition();
            if (pos != null) {
                ChestActor chest = new ChestActor(chestTexture, pos[0], pos[1], allWeapons, skin);
                chests.add(chest);
                stage.addActor(chest);
            } else {
                ChestActor chest = new ChestActor(chestTexture, 500, mapManager.settings.groundMax + 10, allWeapons,skin);
                chests.add(chest);
                stage.addActor(chest);
            }
        }
    }

    // Example of moving your getRandomSpawnPosition logic into GameWorld
    private float[] getRandomSpawnPosition() {
        Array<Actor> candidates = new Array<>();
        for (Actor actor : stage.getActors()) {
            if (actor instanceof PlatformTile) {
                if (actor.getX() > generationSettings.tileWidth
                    && actor.getX() < (generationSettings.roomWidth - generationSettings.tileWidth)) {
                    float spawnX = actor.getX() + actor.getWidth() / 2f;
                    float spawnY = actor.getY() + actor.getHeight();
                    if (!isOverlappingWithDirt(spawnX, spawnY)) {
                        candidates.add(actor);
                    }
                }
            }
        }
        if (candidates.size == 0) {
            return null;
        }
        int index = MathUtils.random(candidates.size - 1);
        Actor chosenTile = candidates.get(index);
        float spawnX = chosenTile.getX() + chosenTile.getWidth() / 2f;
        float spawnY = chosenTile.getY() + chosenTile.getHeight();
        return new float[] { spawnX, spawnY };
    }

    // Example logic method
    private boolean isOverlappingWithDirt(float spawnX, float spawnY) {
        Array<Actor> actors = stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor instanceof DirtTile) {
                float x = actor.getX();
                float y = actor.getY();
                float width = actor.getWidth();
                float height = actor.getHeight();
                if (spawnX >= x && spawnX <= (x + width) &&
                    spawnY >= y && spawnY <= (y + height)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called each frame to update logic like AI, collisions, etc.
     * (If you have advanced AI or collision detection, you can put it here)
     */
    public void update(float delta) {
        // Example: do the normal Stage act() call here, or any
        // special game logic that might occur each frame
        stage.act(delta);
        SoundPhysics.updateDebugEvents(delta);


        // You can do additional logic such as enemy AI, or handle collisions,
        // or handle game events, etc.
    }

    // Getters for anything the GameScreen might need to know
    public PlayerActor getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Stage getStage() {
        return stage;
    }

    // Dispose as needed
    public void dispose() {
        stage.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (dummyTexture != null) dummyTexture.dispose();
        if (chestTexture != null) chestTexture.dispose();
    }

}
