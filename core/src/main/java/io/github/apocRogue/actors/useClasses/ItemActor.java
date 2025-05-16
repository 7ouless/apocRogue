package io.github.apocRogue.actors.useClasses;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Rectangle;
import io.github.apocRogue.weapons.Weapon;

public class ItemActor extends Image {
    private Weapon weapon;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private float gravity = -600f;  //downward acceleration (pixels/sec^2)
    private boolean isOnGround = false;
    private float groundLevel = 0;

    public ItemActor(Weapon weapon, float x, float y) {
        super(weapon.getTexture());
        this.weapon = weapon;
        setPosition(x, y);
        setSize(weapon.getTexture().getWidth(), weapon.getTexture().getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        //if not on ground, apply gravity
        if (!isOnGround) {
            velocityY += gravity * delta;
            float newY = getY() + velocityY * delta;
            float newX = getX() + velocityX * delta;

            //if below ground, place on ground
            if (newY <= groundLevel) {
                newY = groundLevel;
                velocityY = 0;
                isOnGround = true;
            }
            setPosition(newX, newY);
        }
    }


    public void setVelocity(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.isOnGround = false;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
