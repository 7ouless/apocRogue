package io.github.apocRogue.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class BasePlatformTile extends Image {

    public BasePlatformTile(Texture texture, float x, float y) {
        super(new TextureRegionDrawable(new TextureRegion(texture)));
        setPosition(x, y);
        setSize(MapManager.settings.tileWidth, MapManager.settings.tileWidth);
    }
}
