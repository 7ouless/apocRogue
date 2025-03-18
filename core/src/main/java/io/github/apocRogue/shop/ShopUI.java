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

    // UI elements
    private Table itemsTable;
    private ScrollPane itemsScrollPane;
    private Image portraitImage;
    private Label traderDialogLabel;
    private TextButton buyButton;

    // For typing effect
    private String currentLine = "";
    private int displayIndex = 0;
    private float charTimer = 0f;
    private float timeBetweenChars = 0.04f;
    private boolean doneTyping = true;

    // How many items per row in the center panel
    private static final int ITEMS_PER_ROW = 4;

    public ShopUI(Stage stage, Skin skin, List<ShopKeeper> shopkeepers, stageBuilder game) {
        this.skin = skin;
        this.game = game;

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

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

        // Main row: Trader list, Items, Right column n shit
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
                    loadItemsForTrader(trader);
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

        // Right: Portrait + "dialog" box
        Table rightColumn = new Table();

        // Trader Portrait
        Table portraitContainer = new Table();
        portraitContainer.setBackground(createGrayDrawable());
        portraitContainer.defaults().pad(10);

        portraitImage = new Image(new Texture(Gdx.files.internal("ui/portrait-traderA.png")));
        portraitContainer.add(portraitImage).size(200, 200).expand().fill().row();

        rightColumn.add(portraitContainer).expandX().fillX().pad(20).row();

        // Dialog box
        Table dialogContainer = new Table();
        dialogContainer.setBackground(createGrayDrawable());
        dialogContainer.defaults().pad(5);

        Label dialogTitle = new Label("Trader's Dialog", skin);
        dialogContainer.add(dialogTitle).left().row();

        traderDialogLabel = new Label("Select an item, I'll tell you about it!", skin);
        traderDialogLabel.setWrap(true);
        dialogContainer.add(traderDialogLabel).expand().fill().row();

        buyButton = new TextButton("Buy", skin);
        buyButton.setVisible(false);
        dialogContainer.add(buyButton).size(110, 50).pad(10).row();

        rightColumn.add(dialogContainer).width(260).pad(10).row();

        mainRow.add(rightColumn).width(300).expandY().fillY().pad(10);

        // If we have at least one trader, load the first
        if (!shopkeepers.isEmpty()) {
            loadItemsForTrader(shopkeepers.get(0));
        }
    }

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
                buyButton.setVisible(true);
            }
        }
    }


     //Loads this trader's items.

    private void loadItemsForTrader(ShopKeeper trader) {
        itemsTable.clearChildren();
        buyButton.setVisible(false);

        // Cancel any ongoing typing
        doneTyping = true;
        traderDialogLabel.setText("Pick an item!");

        // Update portrait
        portraitImage.setDrawable(trader.getPortrait().getDrawable());

        int count = 0;
        for (ShopItem item : trader.getInventory()) {
            TextButton itemBtn = new TextButton(item.getName(), skin);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // description from ShopInventory
                    startTyping(item.getDescription());
                }
            });
            itemsTable.add(itemBtn);
            count++;
            if (count % ITEMS_PER_ROW == 0) {
                itemsTable.row();
            }
        }
    }

    // Starts typing out the given text from scratch.

    private void startTyping(String text) {
        currentLine = text;
        displayIndex = 0;
        charTimer = 0;
        doneTyping = false;
        traderDialogLabel.setText("");
        buyButton.setVisible(false);
    }

    private Drawable createGrayDrawable() {
        return skin.newDrawable("white", Color.GRAY);
    }
}
