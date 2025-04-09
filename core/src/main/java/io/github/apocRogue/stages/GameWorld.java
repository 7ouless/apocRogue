// =========================
// File: GameWorld.java
// =========================

package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.mobs.FlyingEnemyActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.mobs.DummyActor;
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

public class GameWorld {

    private Stage stage;
    private PlayerActor player;
    private Skin skin;
    private Inventory inventory;
    private MapManager mapManager;
    private ItemManager itemManager;
    private GenerationSettings generationSettings;

    private Array<DummyActor> enemies;
    private Array<FlyingEnemyActor> flyingEnemies;
    private Array<ChestActor> chests;

    private Texture playerTexture;
    private Texture dummyTexture;
    private Texture chestTexture;
    private Texture flyingCreatureTexture;

    private boolean initialized = false;

    public GameWorld(Stage stage) {
        this.stage = stage;
        enemies = new Array<>();
        flyingEnemies = new Array<>();
        chests = new Array<>();
    }

    public void initialize() {
        if (initialized) {
            System.out.println("⚠️ GameWorld.initialize() was already called. Skipping duplicate setup.");
            return;
        }
        initialized = true;

        generationSettings = new GenerationSettings();
        generationSettings.roomWidth = 3000;
        generationSettings.platformDensity = 5;
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        mapManager = new MapManager();
        mapManager.generateMap(stage);

        playerTexture = new Texture("ui/sprite.png");
        dummyTexture = new Texture("ui/dummy.png");
        chestTexture = new Texture("ui/chest.png");
        flyingCreatureTexture = new Texture("ui/bat.png");

        player = new PlayerActor() {
            @Override
            public boolean isOnGround() {
                return super.isOnGround();
            }

            @Override
            public float getVelocityY() {
                return super.getVelocityY();
            }

            @Override
            public void forceEndJumpAnimation() {
                super.forceEndJumpAnimation();
            }
        };

        float spawnY = mapManager.settings.groundMax + 10;
        player.setPosition(50, spawnY);
        stage.addActor(player);

        inventory = new Inventory(skin);
        player.setInventory(inventory);

        itemManager = new ItemManager();
        itemManager.loadFromFile("items.json");
        Array<Weapon> allWeapons = itemManager.getLoadedWeapons();

        int enemyCount = DifficultyLevelGen.getEnemyCount();
        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            DummyActor dummy = (pos != null)
                ? new DummyActor(dummyTexture, pos[0], pos[1])
                : new DummyActor(dummyTexture, 400, mapManager.settings.groundMax + 10);
            enemies.add(dummy);
            stage.addActor(dummy);
        }

        for (int i = 0; i < enemyCount; i++) {
            float[] pos = getRandomSpawnPosition();
            FlyingEnemyActor bat = (pos != null)
                ? new FlyingEnemyActor(flyingCreatureTexture, pos[0], pos[1])
                : new FlyingEnemyActor(flyingCreatureTexture, 400, mapManager.settings.groundMax + 10);
            flyingEnemies.add(bat);
            stage.addActor(bat);
        }

        int chestCount = DifficultyLevelGen.getChestCount();
        for (int i = 0; i < chestCount; i++) {
            float[] pos = getRandomSpawnPosition();
            ChestActor chest = (pos != null)
                ? new ChestActor(chestTexture, pos[0], pos[1], allWeapons, skin)
                : new ChestActor(chestTexture, 500, mapManager.settings.groundMax + 10, allWeapons, skin);
            chests.add(chest);
            stage.addActor(chest);
        }

        System.out.println("✅ GameWorld initialized");
    }

    private float[] getRandomSpawnPosition() {
        Array<Actor> candidates = new Array<>();
        Array<Actor> actorsCopy = new Array<>(stage.getActors());

        for (Actor actor : actorsCopy) {
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

        if (candidates.size == 0) return null;
        int index = MathUtils.random(candidates.size - 1);
        Actor chosenTile = candidates.get(index);
        float spawnX = chosenTile.getX() + chosenTile.getWidth() / 2f;
        float spawnY = chosenTile.getY() + chosenTile.getHeight();
        return new float[]{spawnX, spawnY};
    }

    private boolean isOverlappingWithDirt(float spawnX, float spawnY) {
        Array<Actor> actorsCopy = new Array<>(stage.getActors());
        for (Actor actor : actorsCopy) {
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

    public void update(float delta) {
        stage.act(delta);

        if (!player.isPlayerDead() && player.isOnGround() && player.getVelocityY() <= 0) {
            player.forceEndJumpAnimation();
        }
    }

    public PlayerActor getPlayer() { return player; }
    public Inventory getInventory() { return inventory; }
    public Stage getStage() { return stage; }

    public void dispose() {
        stage.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (dummyTexture != null) dummyTexture.dispose();
        if (chestTexture != null) chestTexture.dispose();
    }
}
