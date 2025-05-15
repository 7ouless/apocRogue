// frontend/src/io/github/apocRogue/shop/ui/TraderUI.java
package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.Map;

public class TraderUI {
    public final String  sellerID;     // "A","B","C"
    public final String  displayName;
    public final Image   portrait;
    public final String[] greetingLines;
    public final String[] thankYouLines;
    public final String[] cannotAffordLines;
    public final String[] lockedItemLines;
    public final String[] soldOutLines;

    public TraderUI(String id,
                    String name,
                    String portraitPath,
                    String[] greet,
                    String[] thanks,
                    String[] noGold,
                    String[] locked,
                    String[] soldOut) {
        this.sellerID       = id;
        this.displayName    = name;
        this.portrait       = new Image(new Texture(Gdx.files.internal(portraitPath)));
        this.greetingLines  = greet;
        this.thankYouLines  = thanks;
        this.cannotAffordLines= noGold;
        this.lockedItemLines  = locked;
        this.soldOutLines     = soldOut;
    }
}
