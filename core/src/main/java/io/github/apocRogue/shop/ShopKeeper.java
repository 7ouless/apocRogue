package io.github.apocRogue.shop;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import java.util.List;

public class ShopKeeper {
    private String name;
    private Image portrait;
    private List<ShopItem> inventory;

    private int level = 1;
    private int goldSpent = 0;

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

    public int getLevel() {
        return level;
    }

    public int getGoldSpent() {
        return goldSpent;
    }

    public void buyItem(ShopItem item) {
        goldSpent += item.getPrice(); //temporary, only increments goldSpent

        // Check if we cross the 10,000 threshold
        if (goldSpent >= 10000 && level == 1) {
            level = 2;
        }
    }

    // Personality lines
    // These are meant to be overridden by each trader
    public String getGreeting() {
        return "Welcome! Good to see you.";
    }

    public String getThankYouLine() {
        return "Thank you for your purchase!";
    }

    public String getCannotAffordLine() {
        return "You don't have enough gold, friend.";
    }

    public String getLockedItemLine() {
        return "Hmm, you seem unworthy to buy that just yet...";
    }
}
