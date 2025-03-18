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

    private Table itemsTable;
    private ScrollPane itemsScrollPane;
    private Image portraitImage;
    private Label itemDescriptionLabel;
    private TextButton buyButton;

    // How many items per row in the center panel
    private static final int ITEMS_PER_ROW = 4;

    public ShopUI(Stage stage, Skin skin, List<ShopKeeper> shopkeepers, stageBuilder game) {
        this.skin = skin;
        this.game = game;

        // Hopefully switches screen instead of overlapping menu

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);


        // Top row: "Back" button, pinned top-left
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to main menu
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(backButton).left().pad(10);
        root.row(); // move layout to next row maybe (not tested yet)


        // Main row has 3 columns: 1) Trader list   2) Items   3) Portrait + desc

        Table mainRow = new Table();
        root.add(mainRow).expand().fill().row();

        // COL 1: Trader List
        Table traderListContainer = new Table();
        // Grey background for the entire trader list area
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

        ScrollPane traderListScroll = new ScrollPane(traderListTable, skin);
        traderListScroll.setScrollingDisabled(true, false);
        traderListContainer.add(traderListScroll).expand().fill();

        mainRow.add(traderListContainer).width(200).expandY().fillY().pad(10);

        //COL 2: Items
        Table itemsContainer = new Table();
        itemsContainer.setBackground(createGrayDrawable());

        itemsTable = new Table();

        itemsTable.defaults().size(120, 80).pad(5);

        itemsScrollPane = new ScrollPane(itemsTable, skin);
        itemsScrollPane.setScrollingDisabled(false, false);

        itemsContainer.add(itemsScrollPane).expand().fill().pad(10);

        mainRow.add(itemsContainer).expand().fill().pad(10);

        //COL 3: Right Column
        // I didn't set a background on this entire column, so black remains visible behind the squares inside it.
        Table rightColumn = new Table();

        // Portrait Square
        Table portraitContainer = new Table();
        // Grey square for the trader portrait
        portraitContainer.setBackground(createGrayDrawable());
        portraitContainer.defaults().pad(10);

        portraitImage = new Image(new Texture(Gdx.files.internal("ui/portrait-traderA.png")));

        portraitContainer.add(portraitImage).size(200, 200).expand().fill().row();

        // Add portrait container (top)
        rightColumn.add(portraitContainer)
            .expandX().fillX()
            .pad(20)
            .row();

        // Description Square
        // This is a separate grey square below the portrait square
        Table descContainer = new Table();
        descContainer.setBackground(createGrayDrawable());
        descContainer.defaults().pad(5);

        Label descTitleLabel = new Label("Item Description", skin);
        descContainer.add(descTitleLabel).left().row();

        itemDescriptionLabel = new Label("Select an item", skin);
        itemDescriptionLabel.setWrap(true);
        descContainer.add(itemDescriptionLabel).expand().fill().row();

        buyButton = new TextButton("Buy", skin);
        buyButton.setVisible(false);

        buyButton.getLabel().setFontScale(1.2f);
        descContainer.add(buyButton).size(110, 50).pad(10).row();


        rightColumn.add(descContainer)
            .width(260)
            .pad(10)
            .row();

        // Add the entire right column to mainRow
        mainRow.add(rightColumn).width(300).expandY().fillY().pad(10);


        // If we have at least one trader, load the first by default
        if (!shopkeepers.isEmpty()) {
            loadItemsForTrader(shopkeepers.get(0));
        }
    }


     //Load the items for this trader into the middle grid, update portrait n shit.

    private void loadItemsForTrader(ShopKeeper trader) {
        itemsTable.clearChildren();
        itemDescriptionLabel.setText("Select an item");
        buyButton.setVisible(false);

        // Update the portrait
        portraitImage.setDrawable(trader.getPortrait().getDrawable());

        // Place items left to right until reaching ITEMS_PER_ROW, then row
        int count = 0;
        for (ShopItem item : trader.getInventory()) {
            TextButton itemBtn = new TextButton(item.getName(), skin);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    showItemDetails(item);
                }
            });
            itemsTable.add(itemBtn);

            count++;
            if (count % ITEMS_PER_ROW == 0) {
                itemsTable.row();
            }
        }
    }


     // Show details & enable Buy button

    private void showItemDetails(ShopItem item) {
        itemDescriptionLabel.setText(
            item.getName() + "\n" +
                item.getDescription() + "\n" +
                "Price: " + item.getPrice()
        );
        buyButton.setVisible(true);
    }


     // Creates a simple grey drawable for panel backgrounds

    private Drawable createGrayDrawable() {
        return skin.newDrawable("white", Color.GRAY);
    }
}
