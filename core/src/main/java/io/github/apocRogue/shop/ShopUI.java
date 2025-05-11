package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.*;
import java.util.ArrayList;
import java.util.List;
import io.github.apocRogue.shop.ShopService;
import io.github.apocRogue.shop.ShopWeaponPayload;
import com.badlogic.gdx.utils.Array;

public class ShopUI {
    private stageBuilder game;
    private Skin skin;

    private Table itemsTable;
    private ScrollPane itemsScrollPane;
    private Image portraitImage;
    private Label traderDialogLabel;
    private Label spentLabel;
    private Label restockLabel; // Countdown label for restock

    private TextButton buyButton;
    private TextButton buyAgainButton;

    // Typewriter logic
    private String currentLine = "";
    private int displayIndex = 0;
    private float charTimer = 0f;
    private float timeBetweenChars = 0.04f;
    private boolean doneTyping = true;

    private boolean showBuyButtonAfterTyping = false;
    private boolean showBuyAgainButtonAfterTyping = false;

    // Grid logic
    private static final int ITEMS_PER_ROW = 3;
    private ShopKeeper currentTrader;
    private ShopItem selectedItem;
    private final Inventory playerInventory;

    // Restock logic
    private float restockTimer = 200f;
    private List<ShopKeeper> allTraders;

    // ID system support
    private final WeaponTypeRegistry typeRegistry;
    private final List<Weapon> loadedWeapons = new ArrayList<>();

    public ShopUI(Stage stage,
                  Skin skin,
                  List<ShopKeeper> shopkeepers,
                  stageBuilder game,
                  Inventory playerInventory)
    {
        this.skin            = skin;
        this.game            = game;
        this.allTraders      = shopkeepers;
        this.playerInventory = playerInventory;

        // ─── Build root UI ───────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        restockLabel = new Label("Next Restock: 20s", skin);
        root.add(restockLabel).center().pad(10);
        root.row();

        spentLabel = new Label("Spent: 0 / 10000", skin);
        root.add(spentLabel).expandX().right().pad(10);
        root.row();

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(backButton).left().pad(10);
        root.row();

        Table mainRow = new Table();
        root.add(mainRow).expand().fill().row();

        // ─── Left: Trader list ────────────────────────────────────
        Table traderListContainer = new Table(skin);
        traderListContainer.setBackground(createGrayDrawable());
        Table traderListTable = new Table(skin);
        traderListTable.defaults().pad(10f);
        for (ShopKeeper trader : allTraders) {
            TextButton traderBtn = new TextButton(trader.getName(), skin);
            traderBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    loadTraderAndGreet(trader);
                }
            });
            traderListTable.add(traderBtn).expandX().fillX().row();
        }
        ScrollPane traderScroll = new ScrollPane(traderListTable, skin);
        traderScroll.setScrollingDisabled(true, false);
        traderListContainer.add(traderScroll).expand().fill();
        mainRow.add(traderListContainer).width(200).expandY().fillY().pad(10);

        // ─── Center: Items Grid ──────────────────────────────────
        Table itemsContainer = new Table(skin);
        itemsContainer.setBackground(createGrayDrawable());
        itemsTable = new Table(skin);
        itemsTable.defaults().size(120,80).pad(5);
        itemsScrollPane = new ScrollPane(itemsTable, skin);
        itemsScrollPane.setScrollingDisabled(false, false);
        itemsContainer.add(itemsScrollPane).expand().fill().pad(10);
        mainRow.add(itemsContainer).expand().fill().pad(10);

        // ─── Right: Portrait + Dialog + Buttons ─────────────────
        Table rightColumn = new Table(skin);

        Table portraitContainer = new Table(skin);
        portraitContainer.setBackground(createGrayDrawable());
        portraitImage = new Image(new Texture(Gdx.files.internal("ui/portrait-traderA.png")));
        portraitContainer.add(portraitImage).size(200,200).expand().fill().row();
        rightColumn.add(portraitContainer).expandX().fillX().pad(20).row();

        Table dialogContainer = new Table(skin);
        dialogContainer.setBackground(createGrayDrawable());
        dialogContainer.defaults().pad(5);

        Label dialogTitle = new Label("Trader's Dialog", skin);
        dialogContainer.add(dialogTitle).left().row();

        traderDialogLabel = new Label("Select an item, I'll tell you about it!", skin);
        traderDialogLabel.setWrap(true);
        dialogContainer.add(traderDialogLabel).expand().fill().row();

        buyButton = new TextButton("Buy", skin);
        buyButton.setVisible(false);
        buyButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                ShopItem itemRef = selectedItem;
                if (itemRef == null || currentTrader == null) return;

                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                    return;
                }
                if (itemRef.getRequiredLevel() > currentTrader.getLevel()) {
                    showDialog(currentTrader.getLockedItemLine(), false, false);
                    return;
                }

                currentTrader.buyItem(itemRef);
                updateSpentLabel();
                rebuildItemGrid();

                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                } else {
                    showDialog(currentTrader.getThankYouLine(), false, true);
                }

                // --- OLD: itemManager.getLoadedWeapons() → NEW: loadedWeapons
                Weapon purchased = null;
                for (Weapon w : loadedWeapons) {
                    if (w.getName().equals(itemRef.getName())) {
                        purchased = w;
                        break;
                    }
                }
                if (purchased != null) {
                    playerInventory.addItem(purchased);
                    InventoryPreferences.add(itemRef.getName());
                }
            }
        });
        dialogContainer.add(buyButton).size(110,50).pad(10).row();

        buyAgainButton = new TextButton("Buy Again", skin);
        buyAgainButton.setVisible(false);
        buyAgainButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                ShopItem itemRef = selectedItem;
                if (itemRef == null || currentTrader == null) return;
                if (itemRef.getStock() <= 0 ||
                    itemRef.getRequiredLevel() > currentTrader.getLevel())
                {
                    // handle errors
                    return;
                }
                currentTrader.buyItem(itemRef);
                updateSpentLabel();
                rebuildItemGrid();
                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                }
            }
        });
        dialogContainer.add(buyAgainButton).size(110,50).pad(10).row();

        rightColumn.add(dialogContainer).width(260).pad(10).row();
        mainRow.add(rightColumn).width(300).expandY().fillY().pad(10);

        // ─── NEW: Initialize ID system registry & weapon list ─────
        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");

        // ─── Ask backend for today’s inventory ─────────────────────

        ShopService api = new ShopService();
        api.fetchShopWeapons(new ShopService.Callback() {
            @Override public void onSuccess(Array<ShopWeaponPayload> list) {
                loadedWeapons.clear();
                for (ShopWeaponPayload p : list) {
                    WeaponTypeInfo info = typeRegistry.get(p.typeID);

                    Weapon w = new Weapon(
                        p.itemCode,
                        info.getName(),
                        p.stats.get("damage"),
                        new Texture(Gdx.files.internal(info.getTexturePath())),
                        info.isProjectileType(),
                        p.stats.get("projectileValue"),
                        info.getAmmoTexture(),
                        p.stats.get("animationSpeed"),
                        p.stats.get("noiseLevel"),
                        p.stats.get("dashSpeed"),
                        p.stats.get("dashDuration"),
                        p.stats.get("dashCooldown")
                    );
                    loadedWeapons.add(w);
                }

                // now that we *have* weapons we can open the first trader
                if (!allTraders.isEmpty()) loadTraderAndGreet(allTraders.get(0));
            }
            @Override public void onFailure(Throwable t) {
                Gdx.app.error("SHOP", "Could not fetch shop items", t);
            }
        });
        // ─── Load first trader’s view ─────────────────────────────
        if (!allTraders.isEmpty()) {
            loadTraderAndGreet(allTraders.get(0));
        }
    }


    public void update(float delta) {
        // Typewriter effect...
        if (!doneTyping) {
            charTimer += delta;
            while (charTimer > timeBetweenChars && displayIndex < currentLine.length()) {
                displayIndex++;
                charTimer -= timeBetweenChars;
                traderDialogLabel.setText(currentLine.substring(0, displayIndex));
            }
            if (displayIndex >= currentLine.length()) {
                doneTyping = true;
                buyButton.setVisible(showBuyButtonAfterTyping);
                buyAgainButton.setVisible(showBuyAgainButtonAfterTyping);
            }
        }
        // Restock...
        restockTimer -= delta;
        if (restockTimer <= 0) {
            for (ShopKeeper sk : allTraders) sk.restock();
            restockTimer = 200f;
            if (currentTrader != null) rebuildItemGrid();
            showDialog("All items have been restocked!", false, false);
        }
        restockLabel.setText("Next Restock: " + (int)Math.ceil(restockTimer) + "s");
    }

    private void loadTraderAndGreet(ShopKeeper trader) {
        currentTrader = trader;
        selectedItem  = null;
        showDialog(trader.getGreeting(), false, false);
        portraitImage.setDrawable(trader.getPortrait().getDrawable());
        updateSpentLabel();
        rebuildItemGrid();
    }

    private void rebuildItemGrid() {
        if (currentTrader == null) return;
        itemsTable.clearChildren();
        int count = 0;
        for (ShopItem item : currentTrader.getInventory()) {
            String labelText;
            if (item.getStock() <= 0) {
                labelText = "SOLD OUT";
            } else if (item.getRequiredLevel() > currentTrader.getLevel()) {
                labelText = "???";
            } else {
                labelText = item.getName() + "\nStock: " + item.getStock();
            }
            TextButton itemBtn = new TextButton(labelText, skin);
            itemBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    selectedItem = item;
                    if (item.getStock() <= 0) {
                        showDialog(currentTrader.getSoldOutLine(), false, false);
                    } else if (item.getRequiredLevel() > currentTrader.getLevel()) {
                        showDialog(currentTrader.getLockedItemLine(), false, false);
                    } else {
                        showDialog(item.getDescription(), true, false);
                    }
                }
            });
            itemsTable.add(itemBtn);
            if (++count % ITEMS_PER_ROW == 0) itemsTable.row();
        }
    }

    private void updateSpentLabel() {
        if (currentTrader != null) {
            spentLabel.setText("Spent: " + currentTrader.getGoldSpent() + " / 10000");
        }
    }

    private void showDialog(String text, boolean showBuy, boolean showBuyAgain) {
        currentLine = text;
        displayIndex = 0;
        charTimer = 0f;
        doneTyping = false;
        traderDialogLabel.setText("");
        buyButton.setVisible(false);
        buyAgainButton.setVisible(false);
        showBuyButtonAfterTyping = showBuy;
        showBuyAgainButtonAfterTyping = showBuyAgain;
    }

    private Drawable createGrayDrawable() {
        return skin.newDrawable("white", Color.GRAY);
    }
}
