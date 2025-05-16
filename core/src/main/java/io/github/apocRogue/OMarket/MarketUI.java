package io.github.apocRogue.OMarket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import io.github.apocRogue.inventory.gameinventory.InventorySlot;
import io.github.apocRogue.inventory.menuinventory.InventoryService;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MarketUI {
    private final Stage stage;
    private final Skin skin;
    private final stageBuilder game;
    private final WeaponTypeRegistry typeRegistry;

    private Table marketTable;
    private Table inventoryTable;
    private final List<InventorySlot> inventorySlots = new ArrayList<>();
    private final Map<InventorySlot, InventoryService.InventoryItemPayload> slotPayloadMap = new HashMap<>();

    public MarketUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage;
        this.skin = skin;
        this.game = game;
        this.typeRegistry = new WeaponTypeRegistry();
        this.typeRegistry.load("ui/weapon_types.json");
    }

    public void build() {
        Table root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Market column
        Table left = new Table(skin);
        left.defaults().pad(5);
        TextButton refresh = new TextButton("Refresh", skin);
        refresh.addListener(new ChangeListener() {
            @Override public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                refreshMarket();
            }
        });
        left.add(refresh).left().row();
        left.add(new Label("Market Listings", skin)).row();

        marketTable = new Table(skin);
        ScrollPane marketScroll = new ScrollPane(marketTable, skin);
        left.add(marketScroll).expand().fill().row();

        TextButton buy = new TextButton("Buy", skin);
        buy.addListener(new ChangeListener() {
            @Override public void changed(ChangeListener.ChangeEvent event, Actor actor) {
            }
        });
        left.add(buy).row();

        root.add(left).expand().fill();

        // Inventory column
        Table right = new Table(skin);
        right.defaults().pad(5);
        right.add(new Label("Your Inventory", skin)).colspan(6).row();
        inventoryTable = new Table(skin);
        inventoryTable.defaults().size(64,64).pad(5);
        right.add(new ScrollPane(inventoryTable, skin)).expand().fill().colspan(6).row();
        Label sellZone = new Label("Drop Here to Sell", skin);
        sellZone.setTouchable(Touchable.enabled);
        right.add(sellZone).colspan(6).padTop(10).row();

        root.add(right).expand().fill();

        DragAndDrop dnd = new DragAndDrop();
        dnd.addTarget(new DragAndDrop.Target(sellZone) {
            @Override public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return payload.getObject() instanceof String;
            }
            @Override public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                promptSell((String)payload.getObject());
            }
        });

        loadInventory(dnd);
        refreshMarket();
    }


    private void promptSell(String code) {
        Dialog dlg = new Dialog("Set Price", skin) {
            @Override protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {
                    int price = Integer.parseInt(
                        ((TextField)getContentTable().findActor("priceField")).getText()
                    );
                    MarketService.sellItem(code, price, new MarketService.Callback<Void>() {
                        @Override public void onSuccess(Void unused) { refreshMarket(); }
                        @Override public void onFailure(Throwable t) {
                            Gdx.app.postRunnable(() -> Gdx.app.error("MarketUI","Sell failed",t));
                        }
                    });
                }
            }
        };
        dlg.getContentTable().add(new Label("Price:", skin));
        TextField tf = new TextField("", skin);
        tf.setName("priceField");
        dlg.getContentTable().add(tf).row();
        dlg.button("OK", true);
        dlg.button("Cancel", false);
        dlg.show(stage);
    }
    private void refreshMarket() {
        MarketService.pullListings(0, 50, null, null, null, null,
            new MarketService.Callback<List<MarketListing>>() {
                @Override public void onSuccess(List<MarketListing> listings) {
                    Gdx.app.postRunnable(() -> {
                        marketTable.clearChildren();
                        int cols = 3, count = 0;
                        for (MarketListing m : listings) {
                            Table cell = new Table(skin);
                            cell.defaults().pad(5);
                            cell.add(new Label(m.getName(), skin)).row();
                            cell.add(new Label("$" + (m.getPrice() / 100.0), skin)).row();

                            // Hover highlight
                            cell.addListener(new InputListener() {
                                @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                                    cell.setColor(1,1,1,0.6f);
                                }
                                @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                                    cell.setColor(1,1,1,1f);
                                }
                            });

                            final long listingId = m.getListingID();
                            cell.addListener(new ClickListener() {
                                @Override public void clicked(InputEvent event, float x, float y) {
                                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                                        Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                                        Gdx.app.log("MarketUI", "buyItem() start: listingId=" + listingId);
                                        MarketService.buyItem(listingId, new MarketService.Callback<Void>() {
                                            @Override public void onSuccess(Void unused) {
                                                refreshMarket();
                                            }
                                            @Override public void onFailure(Throwable t) {
                                                Gdx.app.postRunnable(() -> Gdx.app.error("MarketUI", "Buy failed", t));
                                            }
                                        });
                                    }
                                }
                            });

                            marketTable.add(cell).growX().pad(5);
                            if (++count % cols == 0) marketTable.row();
                        }
                        if (count % cols != 0) marketTable.row();
                    });
                }
                @Override public void onFailure(Throwable t) {
                    Gdx.app.postRunnable(() -> Gdx.app.error("MarketUI", "Refresh failed", t));
                }
            }
        );
    }


    private void loadInventory(DragAndDrop dnd) {
        InventoryService.fetchInventory(new InventoryService.Callback<List<InventoryService.InventoryItemPayload>>() {
            @Override public void onSuccess(List<InventoryService.InventoryItemPayload> items) {
                Gdx.app.postRunnable(() -> {
                    // clear previous state
                    inventorySlots.clear();
                    slotPayloadMap.clear();
                    inventoryTable.clearChildren();

                    // flatten payloads
                    List<InventoryService.InventoryItemPayload> flat = new ArrayList<>();
                    for (InventoryService.InventoryItemPayload p : items) {
                        for (int i = 0; i < p.count; i++) flat.add(p);
                    }

                    int cols = 6, c = 0;
                    for (InventoryService.InventoryItemPayload p : flat) {
                        WeaponTypeInfo info = typeRegistry.get(p.typeID);
                        if (info == null) continue;
                        Weapon w = new Weapon(
                            p.itemCode,
                            info.getName(),
                            p.stats.getOrDefault("damage", 0),
                            new Texture(Gdx.files.internal(info.getTexturePath())),
                            info.isProjectileType(),
                            p.stats.getOrDefault("projectileValue", 0),
                            info.getAmmoTexture(),
                            p.stats.getOrDefault("animationSpeed", 0),
                            p.stats.getOrDefault("noiseLevel", 0),
                            p.stats.getOrDefault("dashSpeed", 0),
                            p.stats.getOrDefault("dashDuration", 0),
                            p.stats.getOrDefault("dashCooldown", 0)
                        );
                        InventorySlot slot = new InventorySlot(skin);
                        slot.setItem(w);
                        inventorySlots.add(slot);
                        slotPayloadMap.put(slot, p);

                        // register drag source with proper capture
                        final InventorySlot s = slot;

                        // Ctrl-click to sell
                        slot.setTouchable(Touchable.enabled);
                        slot.addListener(new ClickListener() {
                            @Override public void clicked(InputEvent event, float x, float y) {
                                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                                    InventoryService.InventoryItemPayload pd = slotPayloadMap.get(s);
                                    if (pd != null) promptSell(pd.itemCode);
                                }
                            }
                        });

                        // Drag source
                        dnd.addSource(new DragAndDrop.Source(slot) {
                            @Override public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                                InventoryService.InventoryItemPayload pd = slotPayloadMap.get(s);
                                if (pd==null) return null;
                                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                                payload.setObject(pd.itemCode);
                                Image img = new Image(s.getItemDrawable());
                                img.setSize(s.getWidth(), s.getHeight());
                                payload.setDragActor(img);
                                return payload;
                            }
                        });

                        inventoryTable.add(slot);
                        if (++c==cols) { inventoryTable.row(); c=0; }
                    }
                    int remainder = flat.size() % 6;
                    if (remainder!=0) {
                        for (int i=remainder;i<6;i++) {
                            InventorySlot ph = new InventorySlot(skin);
                            ph.clearItem();
                            inventoryTable.add(ph);
                        }
                        inventoryTable.row();
                    }
                });
            }

            @Override public void onFailure(Throwable t) {
                Gdx.app.postRunnable(() -> Gdx.app.error("MarketUI","Inventory load failed",t));
            }
        });
    }

}
