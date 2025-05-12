package io.github.apocRogue.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.Json;
import io.github.apocRogue.shop.ShopItemPayload;

public class ItemGenerateService {

    public interface Callback {
        void onSuccess(ShopItemPayload p);
        void onFailure(Throwable t);
    }

    public void generate(String idPrefix, int level, int sub, Callback cb) {
        Json json = new Json();
        String body = json.toJson(new Object() {
            public final String idPrefix_ = idPrefix;
            public final int    level_    = level;
            public final int    sub_      = sub;
        });

        HttpRequest req = new HttpRequest(HttpMethods.POST);
        req.setUrl("https://api.yourgame.com/item/generate");
        req.setContent(body);
        req.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override public void handleHttpResponse(HttpResponse http) {
                try {
                    ShopItemPayload p = json.fromJson(ShopItemPayload.class,
                        http.getResultAsString());
                    cb.onSuccess(p);
                } catch (Exception ex) { cb.onFailure(ex); }
            }
            @Override public void failed(Throwable t) { cb.onFailure(t); }
            @Override public void cancelled() {}
        });
    }
}
