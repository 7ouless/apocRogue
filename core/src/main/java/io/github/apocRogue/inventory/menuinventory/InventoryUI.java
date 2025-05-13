package io.github.apocRogue.inventory.menuinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Scaling;

import io.github.apocRogue.inventory.gameinventory.DragData;
import io.github.apocRogue.inventory.gameinventory.InventorySlot;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Inventory UI that synchronises with the remote Cloud‑Functions back‑end.
 * <p>
 * **Option 1 implemented**: each <strong>itemCode</strong> is now treated as its own visual
 * stack by composing the key <code>typeID + "_" + (projectile?"R":"M")</code>.  Bows and
 * swords (same typeID "01" but different projectile flag) end up in different stash slots.
 */

public class InventoryUI {

    private final Stage               stage;
    private final Skin                skin;
    private final stageBuilder        game;

    private Table root;
    private final List<InventorySlot> stashSlots = new ArrayList<>();
    private final List<InventorySlot> equipSlots = new ArrayList<>();
    private final DragAndDrop         dragAndDrop;

    // ─── ID‑system managers & cache ────────────────────────────────────────────
    private final WeaponTypeRegistry  typeRegistry;
    private final List<Weapon>        loadedWeapons = new ArrayList<>();

    /** Fast lookup that distinguishes projectile/melee variants of the same typeID. */
    private Map<String, Weapon>       weaponByKey = Collections.emptyMap();

    public InventoryUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage;
        this.skin  = skin;
        this.game  = game;

        dragAndDrop = new DragAndDrop();
        buildLayout();

        // ─── Load static weapon metadata (names, textures, etc.) ────────────
        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");
        preloadWeapons();

        // ─── Populate stash from local preferences so the UI is never empty ─
        hydrateFromPreferences();

        // ─── Now pull the definitive truth from the server (asynchronously) ─
        syncWithServerInventory();

        setupDragAndDrop();
    }

    // ------------------------------------------------------------------------
    //  Construction helpers
    // ------------------------------------------------------------------------

    private void buildLayout() {
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Back button --------------------------------------------------------
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(back).left().pad(10);
        root.row();

        // Main content container -------------------------------------------
        Table content = new Table(skin);
        root.add(content).expand().fill().pad(10);
        root.row();

        // Stash grid --------------------------------------------------------
        Table stashTable = new Table(skin);
        stashTable.defaults().size(64,64).pad(5);
        int cols = 6, rows = 3;
        IntStream.range(0, rows).forEach(r -> {
            IntStream.range(0, cols).forEach(c -> {
                InventorySlot slot = new InventorySlot(skin);
                stashSlots.add(slot);
                stashTable.add(slot);
            });
            stashTable.row();
        });
        ScrollPane stashPane = new ScrollPane(stashTable, skin);
        content.add(stashPane).expand().fill().padRight(10);

        // Right column (portrait + equip slots) -----------------------------
        Table rightCol = new Table(skin);
        rightCol.defaults().pad(5);

        Image portrait = new Image(new Texture(Gdx.files.internal("ui/character-portrait.png")));
        portrait.setScaling(Scaling.fit);
        Table portraitTable = new Table(skin);
        portraitTable.setBackground(skin.newDrawable("white", 0.2f,0.2f,0.2f,1f));
        portraitTable.add(portrait).size(200,200);
        rightCol.add(portraitTable).row();

        Table equipTable = new Table(skin);
        equipTable.defaults().size(64,64).pad(5);
        IntStream.range(0, 5).forEach(i -> {
            InventorySlot slot = new InventorySlot(skin);
            equipSlots.add(slot);
            equipTable.add(slot);
        });
        rightCol.add(equipTable);

        content.add(rightCol).width(300).expandY().fillY();
    }

    /**
     * Pre‑loads one <strong>Weapon</strong> stub for every entry in
     * <code>weapon_types.json</code> and registers it in {@link #weaponByKey} using the
     * projectile flag so ranged and melee variants no longer collide.
     */
    private void preloadWeapons() {
        loadedWeapons.clear();
        weaponByKey = new HashMap<>();

        for (String typeID : typeRegistry.getAllTypeIDs()) {
            WeaponTypeInfo info = typeRegistry.get(typeID);
            boolean projectile = info.isProjectileType();

            Weapon w = new Weapon(
                "DUMMY-" + typeID,
                info.getName(),
                0,
                new Texture(Gdx.files.internal(info.getTexturePath())),
                projectile,
                0,
                info.getAmmoTexture(),
                0,0,
                0,0,0
            );
            loadedWeapons.add(w);
            String key = typeID + "_" + (projectile ? "R" : "M");
            weaponByKey.put(key, w);
        }
    }

    private void hydrateFromPreferences() {
        List<String> saved = InventoryPreferences.load();
        for (int i = 0; i < stashSlots.size(); i++) {
            InventorySlot slot = stashSlots.get(i);
            if (i < saved.size()) {
                String name = saved.get(i);
                loadedWeapons.stream()
                    .filter(w -> w.getName().equals(name))
                    .findFirst()
                    .ifPresent(slot::setItem);
            } else {
                slot.clearItem();
            }
        }
    }

    /**
     * Downloads the authoritative inventory list and merges it into the UI.
     */
    private void syncWithServerInventory() {
        InventoryService.fetchInventory(new InventoryService.Callback<List<InventoryService.InventoryItemPayload>>() {
            @Override public void onSuccess(List<InventoryService.InventoryItemPayload> items) {
                stashSlots.forEach(InventorySlot::clearItem);

                for (InventoryService.InventoryItemPayload p : items) {
                    boolean proj = p.stats != null && p.stats.getOrDefault("projectileValue", 0) > 0;
                    String key   = p.typeID + "_" + (proj ? "R" : "M");

                    Weapon base = weaponByKey.get(key);
                    if (base == null) {
                        Gdx.app.error("InventoryUI", "No weapon stub for key "+key);
                        continue;
                    }

                    Weapon w = cloneWithStats(base, p.stats);

                    for (int i = 0; i < p.count; i++) {
                        int idx = findFirstEmptyStash();
                        if (idx >= 0) stashSlots.get(idx).setItem(w);
                    }
                }
                saveStashToPrefs();
            }

            @Override public void onFailure(Throwable t) {
                Gdx.app.error("InventoryUI", "Failed to pull inventory – keeping local copy", t);
            }
        });
    }

    // ------------------------------------------------------------------------
    //  Drag‑and‑drop wiring (unchanged)
    // ------------------------------------------------------------------------

    private Weapon cloneWithStats(Weapon template, Map<String,Integer> stats) {
        if (stats == null) stats = Collections.emptyMap();

        return new Weapon(
            template.getID(),
            template.getName(),
            stats.getOrDefault("damage", template.getDamage()),
            template.getTexture(),
            template.isProjectileType(),
            stats.getOrDefault("projectileValue", template.getProjectileValue()),
            template.getAmmoTexture(),
            stats.getOrDefault("animationSpeed", template.getAnimationSpeed()),
            stats.getOrDefault("noiseLevel", template.getNoiseLevel()),
            stats.getOrDefault("dashSpeed", template.getDashSpeed()),
            stats.getOrDefault("dashDuration", template.getDashDuration()),
            stats.getOrDefault("dashCooldown", template.getDashCooldown())
        );
    }

    private void setupDragAndDrop() {
        iterateAllSlots().forEach(slot -> {
            dragAndDrop.addSource(new Source(slot) {
                @Override public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) return null;
                    DragData dd = new DragData(slot, slot.getWeapon());
                    Payload p  = new Payload();
                    p.setObject(dd);
                    p.setDragActor(new Image(slot.getItemDrawable()));
                    slot.clearItem();
                    return p;
                }
                @Override public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                    if (target == null) {
                        DragData dd = (DragData) payload.getObject();
                        if (dd.sourceSlot != null) dd.sourceSlot.setItem(dd.weapon);
                    }
                    saveStashToPrefs();
                }
            });

            dragAndDrop.addTarget(new Target(slot) {
                @Override public boolean drag(Source source, Payload payload, float x, float y, int pointer) { return true; }
                @Override public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    DragData dd = (DragData) payload.getObject();
                    Weapon incoming = dd.weapon;
                    Weapon existing = slot.getWeapon();
                    slot.setItem(incoming);
                    dd.sourceSlot.setItem(existing);
                    saveStashToPrefs();
                }
            });
        });
    }
    // ------------------------------------------------------------------------
    //  Utility helpers
    // ------------------------------------------------------------------------

    private Iterable<InventorySlot> iterateAllSlots() {
        List<InventorySlot> all = new ArrayList<>();
        all.addAll(stashSlots);
        all.addAll(equipSlots);
        return all;
    }

    private int findFirstEmptyStash() {
        for (int i = 0; i < stashSlots.size(); i++) {
            if (stashSlots.get(i).isEmpty()) return i;
        }
        return -1;
    }

    private void saveStashToPrefs() {
        List<String> names = new ArrayList<>();
        stashSlots.forEach(s -> names.add(s.isEmpty() ? "" : s.getWeapon().getName()));
        InventoryPreferences.save(names);
    }
}
