package io.github.apocRogue.shop;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import java.util.List;

public class ShopKeeper {
    private String name;
    private Image portrait;
    private List<ShopItem> inventory;

    public ShopKeeper(String name, String portraitPath, List<ShopItem> inventory) {
        this.name = name;
        this.inventory = inventory;

        Texture portraitTexture = new Texture(Gdx.files.internal("ui/" + portraitPath + ".png"));
        this.portrait = new Image(portraitTexture);
    }

    public String getName() {
        return name;
    }

    public Image getPortrait() {
        return portrait;
    }

    public List<ShopItem> getInventory() {
        return inventory;
    }
}
