package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.Json;
import io.github.apocRogue.shop.ShopWeaponPayload;

public class WeaponGenerateService {

    public interface Callback {
        void onSuccess(ShopWeaponPayload p);
        void onFailure(Throwable t);
    }

    /** POST /weapon/generate  body:{typeID,skullLevel,skullSub} */
    public void generate(String idPrefix, int skullLevel, int skullSub, Callback cb) {
        Json json = new Json();
        String body = json.toJson(new Object() {
            public final String idPrefix_    = idPrefix;
            public final int    skullLevel_  = skullLevel;
            public final int    skullSub_    = skullSub;
        });

        HttpRequest req = new HttpRequest(HttpMethods.POST);
        req.setUrl("https://api.yourgame.com/weapon/generate");
        req.setContent(body);
        req.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override public void handleHttpResponse(HttpResponse http) {
                try {
                    ShopWeaponPayload p = json.fromJson(ShopWeaponPayload.class,
                        http.getResultAsString());
                    cb.onSuccess(p);
                } catch (Exception ex) { cb.onFailure(ex); }
            }
            @Override public void failed(Throwable t) { cb.onFailure(t); }
            @Override public void cancelled() {}
        });
    }
}
