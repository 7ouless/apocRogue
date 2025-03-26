package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;

import java.util.List;

public class ShopUI {
    private stageBuilder game;
    private Skin skin;

    // Layout elements
    private Table itemsTable;
    private ScrollPane itemsScrollPane;
    private Image portraitImage;
    private Label traderDialogLabel;
    private Label spentLabel; // shows how much gold spent for current trader

    // Buttons for user actions
    private TextButton buyButton;
    private TextButton buyAgainButton;

    // Typewriter logic
    private String currentLine = "";
    private int displayIndex = 0;
    private float charTimer = 0f;
    private float timeBetweenChars = 0.04f;
    private boolean doneTyping = true;

    // Which buttons to show after the typewriter finishes
    private boolean showBuyButtonAfterTyping = false;
    private boolean showBuyAgainButtonAfterTyping = false;

    // Trader / item logic
    private static final int ITEMS_PER_ROW = 3;

    private ShopKeeper currentTrader;
    private ShopItem selectedItem;

    public ShopUI(Stage stage, Skin skin, List<ShopKeeper> shopkeepers, stageBuilder game) {
        this.skin = skin;
        this.game = game;

        // Root table
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // SPENT LABEL (top-right)
        spentLabel = new Label("Spent: 0 / 10000", skin);
        root.add(spentLabel).expandX().right().pad(10);
        root.row();

        // BACK BUTTON (top-left)
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(backButton).left().pad(10);
        root.row();

        // Main row (Trader list, Items, Right column)
        Table mainRow = new Table();
        root.add(mainRow).expand().fill().row();

        // Left: Trader List
        Table traderListContainer = new Table();
        traderListContainer.setBackground(createGrayDrawable());

        Table traderListTable = new Table();
        traderListTable.defaults().pad(10f);

        for (ShopKeeper trader : shopkeepers) {
            TextButton traderBtn = new TextButton(trader.getName(), skin);
            traderBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadTraderAndGreet(trader); // greet on first load
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

        itemsTable.defaults().size(120, 80).pad(5);

        itemsScrollPane = new ScrollPane(itemsTable, skin);
        itemsScrollPane.setScrollingDisabled(false, false);
        itemsContainer.add(itemsScrollPane).expand().fill().pad(10);

        mainRow.add(itemsContainer).expand().fill().pad(10);

        // Right: Portrait + Dialog Box
        Table rightColumn = new Table();

        // Trader Portrait
        Table portraitContainer = new Table();
        portraitContainer.setBackground(createGrayDrawable());
        portraitContainer.defaults().pad(10);

        portraitImage = new Image(new Texture(Gdx.files.internal("ui/portrait-traderA.png")));
        portraitContainer.add(portraitImage).size(200, 200).expand().fill().row();

        rightColumn.add(portraitContainer).expandX().fillX().pad(20).row();

        // Dialog container
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
                if (selectedItem == null || currentTrader == null) return;

                // If item is locked
                if (selectedItem.getRequiredLevel() > currentTrader.getLevel()) {
                    showDialog(currentTrader.getLockedItemLine(), false, false);
                    return;
                }

                // Otherwise buy
                currentTrader.buyItem(selectedItem);

                // If crossing the 10k threshold unlocked level 2, let's instantly refresh
                if (currentTrader.getLevel() == 2) {
                    refreshTraderItems(); // now ??? items become real
                }

                // Show Thank You text, then allow Buy Again
                showDialog(currentTrader.getThankYouLine(), false, true);
            }
        });
        dialogContainer.add(buyButton).size(110, 50).pad(10).row();

        // BUY AGAIN button
        buyAgainButton = new TextButton("Buy Again", skin);
        buyAgainButton.setVisible(false);
        buyAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedItem == null || currentTrader == null) return;
                // Re-buy the same item, no new text
                currentTrader.buyItem(selectedItem);

                // If crossing the threshold unlocked level 2, refresh here too
                if (currentTrader.getLevel() == 2) {
                    refreshTraderItems();
                }

                updateSpentLabel();
            }
        });
        dialogContainer.add(buyAgainButton).size(110, 50).pad(10).row();

        rightColumn.add(dialogContainer).width(260).pad(10).row();
        mainRow.add(rightColumn).width(300).expandY().fillY().pad(10);

        // If we have at least one trader, load the first
        if (!shopkeepers.isEmpty()) {
            loadTraderAndGreet(shopkeepers.get(0));
        }
    }

    //Called every frame. Handles the typewriter effect for the currently displayed text
    public void update(float delta) {
        if (!doneTyping) {
            charTimer += delta;
            while (charTimer > timeBetweenChars && displayIndex < currentLine.length()) {
                displayIndex++;
                charTimer -= timeBetweenChars;
                traderDialogLabel.setText(currentLine.substring(0, displayIndex));
            }
            if (displayIndex >= currentLine.length()) {
                doneTyping = true;
                // Show the relevant buttons after typing completes
                buyButton.setVisible(showBuyButtonAfterTyping);
                buyAgainButton.setVisible(showBuyAgainButtonAfterTyping);
            }
        }
    }

    //Loads the given trader and shows their greeting.
    private void loadTraderAndGreet(ShopKeeper trader) {
        this.currentTrader = trader;
        selectedItem = null;

        // Greet in the typewriter
        showDialog(trader.getGreeting(), false, false);

        // Update portrait
        portraitImage.setDrawable(trader.getPortrait().getDrawable());

        // Update spent label
        updateSpentLabel();

        // Now build the item grid
        rebuildItemGrid();
    }

    //Rebuilds the item grid *without* overwriting the dialog text. This is used after we buy something (to unlock items or see them instantly).
    private void refreshTraderItems() {
        updateSpentLabel();
        rebuildItemGrid();
    }

    // Actually populates the center items table based on currentTrader's inventory. If item is locked, label ???, else actual name.
    private void rebuildItemGrid() {
        itemsTable.clearChildren();
        selectedItem = null; // Clear selection each time
        int count = 0;
        for (ShopItem item : currentTrader.getInventory()) {
            String buttonLabel = (item.getRequiredLevel() > currentTrader.getLevel())
                ? "???" : item.getName();

            TextButton itemBtn = new TextButton(buttonLabel, skin);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectedItem = item;
                    // If locked, show locked line
                    if (item.getRequiredLevel() > currentTrader.getLevel()) {
                        showDialog(currentTrader.getLockedItemLine(), false, false);
                    } else {
                        // Shows item description & allow a 'Buy' after typing
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

    //Updates the label showing how much gold the current trader has received from you.
    private void updateSpentLabel() {
        if (currentTrader != null) {
            spentLabel.setText("Spent: " + currentTrader.getGoldSpent() + " / 10000");
        }
    }

    //Shows text in the dialog box using the typewriter effect, and configures which buttons to appear after typing finishes.
    private void showDialog(String text, boolean showBuy, boolean showBuyAgain) {
        startTyping(text);
        showBuyButtonAfterTyping = showBuy;
        showBuyAgainButtonAfterTyping = showBuyAgain;
    }

    //Initialises the typewriter effect for a new line of text, hiding both buttons while it types.
    private void startTyping(String text) {
        currentLine = text;
        displayIndex = 0;
        charTimer = 0;
        doneTyping = false;
        traderDialogLabel.setText("");

        // Hide buttons during typing
        buyButton.setVisible(false);
        buyAgainButton.setVisible(false);
    }

    //Creates a gray background for the trader list, items panel, etc.
    private Drawable createGrayDrawable() {
        return skin.newDrawable("white", Color.GRAY);
    }
}
