package io.github.apocRogue.potion;

import com.badlogic.gdx.graphics.Texture;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class Potion {
    private final String id;                      // global 3xx+
    private final Map<String,Integer> stats = new LinkedHashMap<>();

    private String  name;
    private Texture texture;
    private boolean stackable;
    private int     maxStack;

    public Potion(String id,
                  String name,
                  Texture texture,
                  boolean stackable,
                  int maxStack) {
        this.id        = id;
        this.name      = name;
        this.texture   = texture;
        this.stackable = stackable;
        this.maxStack  = maxStack;

        stats.put("stackable", stackable ? 1 : 0);
        stats.put("maxStack",  maxStack);
    }

    // ---------- getters ----------
    public String  getID()        { return id; }
    public String  getName()      { return name; }
    public Texture getTexture()   { return texture; }
    public boolean isStackable()  { return stackable; }
    public int     getMaxStack()  { return maxStack; }
    public Map<String,Integer> getStats() { return stats; }

    // ---------- behaviour (stub – implement effect) ----------
    public void use(PlayerActor player, Stage stage) {
        System.out.println("Using potion " + name + " on player " + player);
        // TODO: apply heal, buff, etc. based on stats
    }
}
