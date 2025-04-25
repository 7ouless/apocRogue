package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.inventory.general.ItemManager;
import io.github.apocRogue.inventory.general.InventoryModel;
import io.github.apocRogue.inventory.gameinventory.Inventory;

import java.util.List;

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
    private final ItemManager itemManager;
    private final Inventory playerInventory;

    // Restock logic
    private float restockTimer = 200f;
    private List<ShopKeeper> allTraders;

    public ShopUI(Stage stage, Skin skin, List<ShopKeeper> shopkeepers, stageBuilder game, ItemManager itemManager,
                  Inventory playerInventory) {
        this.skin = skin;
        this.game = game;
        this.allTraders = shopkeepers;
        this.itemManager     = itemManager;
        this.playerInventory = playerInventory;

        // Root layout
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Restock label at top-center
        restockLabel = new Label("Next Restock: 20s", skin);
        root.add(restockLabel).center().pad(10);
        root.row();

        // Spent label top-right
        spentLabel = new Label("Spent: 0 / 10000", skin);
        root.add(spentLabel).expandX().right().pad(10);
        root.row();

        // Back button top-left
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(backButton).left().pad(10);
        root.row();

        // Main row for trader list, items, and right column
        Table mainRow = new Table();
        root.add(mainRow).expand().fill().row();

        // Left: Trader list
        Table traderListContainer = new Table();
        traderListContainer.setBackground(createGrayDrawable());
        Table traderListTable = new Table();
        traderListTable.defaults().pad(10f);

        for (ShopKeeper trader : allTraders) {
            TextButton traderBtn = new TextButton(trader.getName(), skin);
            traderBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadTraderAndGreet(trader);
                }
            });
            traderListTable.add(traderBtn).expandX().fillX().row();
        }
        ScrollPane traderScroll = new ScrollPane(traderListTable, skin);
        traderScroll.setScrollingDisabled(true, false);
        traderListContainer.add(traderScroll).expand().fill();

        mainRow.add(traderListContainer).width(200).expandY().fillY().pad(10);

        // Center: Items Grid
        Table itemsContainer = new Table();
        itemsContainer.setBackground(createGrayDrawable());
        itemsTable = new Table();
        itemsTable.defaults().size(120,80).pad(5);

        itemsScrollPane = new ScrollPane(itemsTable, skin);
        itemsScrollPane.setScrollingDisabled(false, false);
        itemsContainer.add(itemsScrollPane).expand().fill().pad(10);

        mainRow.add(itemsContainer).expand().fill().pad(10);

        // Right: Portrait + dialog
        Table rightColumn = new Table();
        Table portraitContainer = new Table();
        portraitContainer.setBackground(createGrayDrawable());
        portraitContainer.defaults().pad(10);

        portraitImage = new Image(new Texture(Gdx.files.internal("ui/portrait-traderA.png")));
        portraitContainer.add(portraitImage).size(200,200).expand().fill().row();
        rightColumn.add(portraitContainer).expandX().fillX().pad(20).row();

        Table dialogContainer = new Table();
        dialogContainer.setBackground(createGrayDrawable());
        dialogContainer.defaults().pad(5);

        Label dialogTitle = new Label("Trader's Dialog", skin);
        dialogContainer.add(dialogTitle).left().row();

        traderDialogLabel = new Label("Select an item, I'll tell you about it!", skin);
        traderDialogLabel.setWrap(true);
        dialogContainer.add(traderDialogLabel).expand().fill().row();

        // BUY button
        buyButton = new TextButton("Buy", skin);
        buyButton.setVisible(false);
        buyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Save the selected item reference
                ShopItem itemRef = selectedItem;
                if (itemRef == null || currentTrader == null) return;

                // If out of stock
                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                    return;
                }
                // If locked
                if (itemRef.getRequiredLevel() > currentTrader.getLevel()) {
                    showDialog(currentTrader.getLockedItemLine(), false, false);
                    return;
                }

                // Purchase the item
                currentTrader.buyItem(itemRef);
                updateSpentLabel();
                rebuildItemGrid();
                // Restore selection if still in stock
                if (itemRef.getStock() > 0) {
                    selectedItem = itemRef;
                }
                // Show Thank You dialog with the Buy Again button if not sold out
                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                } else {
                    showDialog(currentTrader.getThankYouLine(), false, true);
                }
                Weapon purchased = null;
                for (Weapon w : itemManager.getLoadedWeapons()) {
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
        dialogContainer.add(buyButton).size(110, 50).pad(10).row();

        // BUY AGAIN button
        buyAgainButton = new TextButton("Buy Again", skin);
        buyAgainButton.setVisible(false);
        buyAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Use the last purchased item
                ShopItem itemRef = selectedItem;
                if (itemRef == null || currentTrader == null) return;

                // Check stock and level
                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                    return;
                }
                if (itemRef.getRequiredLevel() > currentTrader.getLevel()) {
                    showDialog(currentTrader.getLockedItemLine(), false, false);
                    return;
                }

                // Silent purchase (no new thank you message)
                currentTrader.buyItem(itemRef);
                updateSpentLabel();
                rebuildItemGrid();
                // Restore selection if still available
                if (itemRef.getStock() > 0) {
                    selectedItem = itemRef;
                }
                // If the item sold out after this purchase, display sold out message
                if (itemRef.getStock() <= 0) {
                    showDialog(currentTrader.getSoldOutLine(), false, false);
                }
                // Otherwise, do nothing so the Buy Again button stays visible for more purchases.
            }
        });
        dialogContainer.add(buyAgainButton).size(110,50).pad(10).row();

        rightColumn.add(dialogContainer).width(260).pad(10).row();
        mainRow.add(rightColumn).width(300).expandY().fillY().pad(10);

        // Load the first trader if available
        if (!allTraders.isEmpty()) {
            loadTraderAndGreet(allTraders.get(0));
        }
    }

    public void update(float delta) {
        // Typewriter effect
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

        // Restock logic
        restockTimer -= delta;
        if (restockTimer <= 0) {
            // Restock all traders
            for (ShopKeeper sk : allTraders) {
                sk.restock();
            }
            restockTimer = 200f;
            if (currentTrader != null) {
                rebuildItemGrid();
            }
            showDialog("All items have been restocked!", false, false);
        }

        int secs = (int)Math.ceil(restockTimer);
        restockLabel.setText("Next Restock: " + secs + "s");
    }

    private void loadTraderAndGreet(ShopKeeper trader) {
        currentTrader = trader;
        selectedItem = null;
        showDialog(trader.getGreeting(), false, false);
        portraitImage.setDrawable(trader.getPortrait().getDrawable());
        updateSpentLabel();
        rebuildItemGrid();
    }

    private void rebuildItemGrid() {
        if (currentTrader == null) return;
        itemsTable.clearChildren();
        // Note: We intentionally do not clear 'selectedItem' here so that a previous purchase can be repeated.
        int count = 0;
        for (ShopItem item : currentTrader.getInventory()) {
            String labelText;
            if (item.getStock() <= 0) {
                labelText = "SOLD OUT";
            } else if (item.getRequiredLevel() > currentTrader.getLevel()) {
                labelText = "???";
            } else {
                // Display item name and current stock
                labelText = item.getName() + "\nStock: " + item.getStock();
            }

            TextButton itemBtn = new TextButton(labelText, skin);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectedItem = item;
                    if (item.getStock() <= 0) {
                        showDialog(currentTrader.getSoldOutLine(), false, false);
                    } else if (item.getRequiredLevel() > currentTrader.getLevel()) {
                        showDialog(currentTrader.getLockedItemLine(), false, false);
                    } else {
                        // Show item description and enable the Buy button
                        showDialog(item.getDescription(), true, false);
                    }
                }
            });
            itemsTable.add(itemBtn);
            count++;
            if (count % ITEMS_PER_ROW == 0) {
                itemsTable.row();
            }
        }
    }

    private void updateSpentLabel() {
        if (currentTrader != null) {
            spentLabel.setText("Spent: " + currentTrader.getGoldSpent() + " / 10000");
        }
    }

    private void showDialog(String text, boolean showBuy, boolean showBuyAgain) {
        startTyping(text);
        showBuyButtonAfterTyping = showBuy;
        showBuyAgainButtonAfterTyping = showBuyAgain;
    }

    private void startTyping(String text) {
        currentLine = text;
        displayIndex = 0;
        charTimer = 0;
        doneTyping = false;
        traderDialogLabel.setText("");
        buyButton.setVisible(false);
        buyAgainButton.setVisible(false);
    }

    private Drawable createGrayDrawable() {
        return skin.newDrawable("white", Color.GRAY);
    }
}
