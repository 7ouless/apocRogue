package io.github.apocRogue.inventory.menuinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;

import com.badlogic.gdx.utils.Scaling;
import io.github.apocRogue.inventory.gameinventory.DragData;
import io.github.apocRogue.inventory.gameinventory.InventorySlot;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.Weapon;

import java.util.ArrayList;
import java.util.List;

public class InventoryUI {
    private final Stage stage;
    private final Skin skin;
    private final stageBuilder game;

    private Table root;
    private List<InventorySlot> stashSlots = new ArrayList<>();
    private List<InventorySlot> equipSlots = new ArrayList<>();
    private DragAndDrop dragAndDrop;

    public InventoryUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage;
        this.skin  = skin;
        this.game  = game;

        dragAndDrop = new DragAndDrop();
        buildLayout();
        setupDragAndDrop();
    }

    private void buildLayout() {
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Header row
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(back).left().pad(10);
        root.row();


        // Main content: two columns
        Table content = new Table(skin);
        root.add(content).expand().fill().pad(10);
        root.row();

        // Left: shit you have grid
        Table stashTable = new Table(skin);
        stashTable.defaults().size(64, 64).pad(5);

        int cols = 6, rows = 3;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                InventorySlot slot = new InventorySlot(skin);
                stashSlots.add(slot);
                stashTable.add(slot);
            }
            stashTable.row();
        }

        ScrollPane stashPane = new ScrollPane(stashTable, skin);
        content.add(stashPane).expand().fill().padRight(10);

        // Right side: Character + equip slots
        Table rightCol = new Table(skin);
        rightCol.defaults().pad(5);

        // 1) Portrait area on top
        Image portrait = new Image(new Texture(Gdx.files.internal("ui/character-portrait.png")));
        portrait.setScaling(Scaling.fit);
        Table portraitTable = new Table(skin);
        portraitTable.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f));
        portraitTable.add(portrait).size(200, 200);
        rightCol.add(portraitTable).row();

        // 2) Equip area below
        Table equipTable = new Table(skin);
        equipTable.defaults().size(64, 64).pad(5);
        for (int i = 0; i < 5; i++) {
            InventorySlot slot = new InventorySlot(skin);
            equipSlots.add(slot);
            equipTable.add(slot);
        }
        rightCol.add(equipTable);

        content.add(rightCol).width(300).expandY().fillY();

    }

    private void setupDragAndDrop() {
        for (final InventorySlot slot : iterateAllSlots()) {

            dragAndDrop.addSource(new Source(slot) {
                @Override
                public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) return null;
                    Payload p = new Payload();
                    p.setObject(new DragData(slot, slot.getWeapon()));
                    Image dragImg = new Image(slot.getItemDrawable());
                    p.setDragActor(dragImg);
                    slot.clearItem();
                    return p;
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                    if (target == null) {
                        DragData dd = (DragData)payload.getObject();
                        if (dd.sourceSlot != null) dd.sourceSlot.setItem(dd.weapon);
                    }
                }
            });


            dragAndDrop.addTarget(new Target(slot) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    return true;
                }

                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    DragData dd = (DragData)payload.getObject();
                    if (dd == null) return;
                    Weapon incoming = dd.weapon;
                    Weapon existing = slot.getWeapon();
                    slot.setItem(incoming);
                    dd.sourceSlot.setItem(existing);
                }
            });
        }
    }

    private Iterable<InventorySlot> iterateAllSlots() {
        List<InventorySlot> all = new ArrayList<>();
        all.addAll(stashSlots);
        all.addAll(equipSlots);
        return all;
    }
}
