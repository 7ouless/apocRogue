package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;

import java.util.List;

/**
 * Front‑end shop screen that talks to the back‑end Cloud Functions.
 * Uses front‑end {@link ShopKeeper} instances only for visuals (portrait & dialog).
 */
public class ShopUI {
    private final Stage stage;
    private final Skin skin;
    private final stageBuilder game;

    private final Table itemsTable;
    private final Label dialogLabel;
    private final Label spentLabel;
    private final Image portraitImage;
    private final TextButton buyBtn;

    // list of visual traders shipped with the APK
    private final List<ShopKeeper> traders = ShopInventory.loadShopkeepers();

    private ShopKeeper currentTrader;   // purely visual
    private SellerInfo currentInfo;     // data from BE
    private ShopEntry  selectedEntry;

    private final ShopService api = new ShopService();

    public ShopUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage; this.skin = skin; this.game = game;

        /* ---------- layout ---------- */
        Table root = new Table(skin); root.setFillParent(true); stage.addActor(root);

        spentLabel = new Label("Reputation: --", skin);
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener(){@Override public void clicked(InputEvent e, float x, float y){game.setScreen(new MainScreen(game));}});
        root.add(spentLabel).left().pad(5); root.add().expandX(); root.add(back).right().pad(5); root.row();

        Table main = new Table(skin); root.add(main).expand().fill().colspan(3); main.row();

        /* Traders column */
        Table left = new Table(skin);
        for (ShopKeeper t : traders) {
            TextButton b = new TextButton(t.getDisplayName(), skin);
            b.addListener(new ClickListener(){@Override public void clicked(InputEvent e, float x, float y){selectTrader(t);}});
            left.add(b).expandX().fillX().pad(4).row();
        }
        main.add(left).width(200).fillY().pad(5);

        /* Items grid */
        itemsTable = new Table(skin); itemsTable.defaults().size(100,100).pad(4);
        ScrollPane scroll = new ScrollPane(itemsTable, skin);
        main.add(scroll).expand().fill().pad(5);

        /* Detail column */
        Table right = new Table(skin);
        portraitImage = new Image();
        Label nameLbl = new Label("", skin);
        dialogLabel = new Label("", skin); dialogLabel.setWrap(true);
        buyBtn = new TextButton("Buy", skin); buyBtn.setDisabled(true);
        buyBtn.addListener(new ClickListener(){@Override public void clicked(InputEvent e, float x, float y){buySelected();}});
        right.add(portraitImage).size(200,200).row();
        right.add(nameLbl).pad(5).row();
        right.add(dialogLabel).width(200).height(100).pad(5).row();
        right.add(buyBtn).pad(5);
        main.add(right).width(220).fillY().pad(5);

        /* load first */
        if(!traders.isEmpty()) selectTrader(traders.get(0));
    }

    private void selectTrader(ShopKeeper t) {
        currentTrader = t; selectedEntry = null; buyBtn.setDisabled(true);
        // portrait
        portraitImage.setDrawable(new TextureRegionDrawable(new Texture(Gdx.files.internal(t.getPortraitPath()))));
        dialogLabel.setText(t.getGreeting());
        spentLabel.setText("Reputation: --");

        // ask back‑end
        api.fetchSeller(t.getSellerID(), new ShopService.Callback<SellerInfo>() {
            @Override public void onSuccess(final SellerInfo info) {
                Gdx.app.postRunnable(() -> {
                    currentInfo = info;
                    spentLabel.setText("Reputation: " + info.spentGauge);
                    rebuildGrid();
                });
            }
            @Override public void onFailure(final Throwable thr) {
                Gdx.app.postRunnable(() ->
                    dialogLabel.setText("Failed to load: " + thr.getMessage()));
            }
        });
    }

    private void rebuildGrid() {
        itemsTable.clearChildren();
        if (currentInfo == null) return;

        int col = 0;
        for (ShopEntry e : currentInfo.items) {
            Table cell = new Table(skin);
            cell.setTouchable(Touchable.enabled);   // ensure it receives clicks

            Texture tex = new Texture(Gdx.files.internal(e.texturePath));
            Image   img = new Image(tex);
            Label   name = new Label(e.name, skin);
            Label   price = new Label("Price: " + e.price, skin);
            Label   left  = new Label("Left: "  + e.remaining, skin);

            cell.add(img).size(64,64).row();
            cell.add(name).padTop(2).row();
            cell.add(price).row();
            cell.add(left);

            cell.addListener(new ClickListener(){@Override public void clicked(InputEvent ev,float x,float y){selectEntry(e);}});

            itemsTable.add(cell).pad(4);
            if (++col % 3 == 0) itemsTable.row();
        }
    }

    private void selectEntry(ShopEntry e) {
        selectedEntry = e;

        StringBuilder sb = new StringBuilder();
        if (e.stats != null && !e.stats.isEmpty()) {
            e.stats.forEach((k,v) -> sb.append(k).append(": ").append(v).append('\n'));
            sb.append('\n');
        }
        sb.append("Price: ").append(e.price);

        dialogLabel.setText(sb.toString());
        buyBtn.setDisabled(e.remaining <= 0);
    }

    private void buySelected(){
        if(currentInfo==null||selectedEntry==null) return;
        api.buyItem(currentInfo.sellerID, selectedEntry.itemCode, 1,
            new ShopService.Callback<Void>() {
                @Override public void onSuccess(Void v){
                    selectedEntry.remaining--; buyBtn.setDisabled(selectedEntry.remaining<=0);
                    dialogLabel.setText(selectedEntry.remaining<=0?currentTrader.getSoldOutLine():currentTrader.getThankYouLines()[0]);
                    spentLabel.setText("Reputation: "+(currentInfo.spentGauge+1));
                }
                @Override public void onFailure(Throwable t){ dialogLabel.setText("Purchase failed: "+t.getMessage()); }
            });
    }
}
