package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;

public class ShopService {

    public interface Callback {
        void onSuccess(Array<ShopWeaponPayload> list);
        void onFailure(Throwable t);
    }

    public void fetchShopWeapons(Callback cb) {
        HttpRequest req = new HttpRequest(HttpMethods.GET);
        req.setUrl("https://api.yourgame.com/shop");   // ← point to your backend

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override public void handleHttpResponse(HttpResponse http) {
                try {
                    String json = http.getResultAsString();
                    Json j = new Json();
                    @SuppressWarnings("unchecked")
                    Array<ShopWeaponPayload> arr =
                        j.fromJson(Array.class, ShopWeaponPayload.class, json);
                    cb.onSuccess(arr);
                } catch (Exception ex) { cb.onFailure(ex); }
            }
            @Override public void failed(Throwable t) { cb.onFailure(t); }
            @Override public void cancelled() {}
        });
    }
}
