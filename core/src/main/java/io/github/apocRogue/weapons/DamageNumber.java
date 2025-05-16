package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class DamageNumber extends Label {
    private float timeAlive = 0f;
    private float maxTime = 1f; // fade out after 1 second

    public DamageNumber(CharSequence text, Skin skin, float x, float y) {
        super(text, skin);
        setPosition(x, y);
        setColor(Color.WHITE);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeAlive += delta;

        //offset - to remove at a later date
        setY(getY() + 20 * delta);

        float alpha = 1f - (timeAlive / maxTime);
        setColor(getColor().r, getColor().g, getColor().b, alpha);

        if (timeAlive >= maxTime) {
            remove();
        }
    }
}
