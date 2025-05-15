package io.github.apocRogue.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import io.github.apocRogue.database.ServerSingleton;
import io.github.apocRogue.shop.SellerInfo;

import java.util.HashMap;
import java.util.Map;

import static io.github.apocRogue.database.DBManager.GSON;

/**
 * Client‑side helper that talks to the Cloud‑Functions shop API.
 * <p>
 * Mirrors the style used in {@link io.github.apocRogue.database.DBManager} so the
 * <strong>Authorization: Bearer &lt;token&gt;</strong> header is automatically sent on every
 * request, exactly the way your other services already do.
 */
public class ShopService {
    public interface Callback<T> {
        void onSuccess(T value);
        void onFailure(Throwable t);
    }

    private static final String BASE =
        "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    private final Json json = new Json();

    /* ────────────────────────────────
     *  GET /dailyshop?seller=NPC_ID
     * ──────────────────────────────── */
    public void fetchSeller(String sellerId, Callback<SellerInfo> cb) {
        String url = BASE + "/dailyshop?seller=" + sellerId;

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.GET)
            .url(url)
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setTimeOut(10_000); // 10 s

        Gdx.net.sendHttpRequest(req, handler(cb, SellerInfo.class));
    }

    /* ────────────────────────────────
     *  POST /buyshopitem { sellerID, itemCode, count }
     * ──────────────────────────────── */
    public void buyItem(String sellerId, String itemCode, int count, Callback<Void> cb) {
        String url = BASE + "/buyshopitem";

        Map<String,Object> body = new HashMap<>();
        body.put("sellerID", sellerId);
        body.put("itemCode", itemCode);
        body.put("count",    count);
        String jsonBody = GSON.toJson(body);

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setContent(jsonBody);
        req.setTimeOut(10_000);

        Gdx.net.sendHttpRequest(req, handler(cb, Void.class));
    }

    /* ---------- shared response handler ---------- */
    private <T> Net.HttpResponseListener handler(Callback<T> cb, Class<T> typ) {
        return new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse resp) {
                int status = resp.getStatus().getStatusCode();
                String raw = resp.getResultAsString();
                Gdx.app.log("SHOP-RAW", raw);

                if (status != 200) { cb.onFailure(new Exception("HTTP "+status+" — "+raw)); return; }

                try {
                    if (typ == Void.class) {          // <── add this guard
                        cb.onSuccess(null);
                    } else {
                        T parsed = json.fromJson(typ, raw);
                        cb.onSuccess(parsed);
                    }
                } catch (Exception ex) {
                    cb.onFailure(ex);
                }
            }

            @Override public void failed(Throwable t)    { cb.onFailure(t); }
            @Override public void cancelled()             { cb.onFailure(new Exception("cancelled")); }
        };
    }
}
