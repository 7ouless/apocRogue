package io.github.apocRogue.inventory.menuinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.Json;
import io.github.apocRogue.database.ServerSingleton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Throwable t);
    }

    // TODO: Point this at your real Cloud Function URL (or inject via config)
    private static final String BASE_URL = "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    /** Matches the JSON structure returned by InventoryPull */
    public static class InventoryItemPayload {
        public String              itemCode;
        public String              typeID;
        public int                 skullLevel;
        public int                 skullSub;
        public HashMap<String,Integer> stats;
        public int                 count;
    }

    /** Fetch the full inventory for the current player. */
    public static void fetchInventory(Callback<List<InventoryItemPayload>> cb) {
        HttpRequest req = new HttpRequest(HttpMethods.GET);
        req.setUrl(BASE_URL + "/inventorypull");
        // If you require auth:
        req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                try {
                    int code = response.getStatus().getStatusCode();

                    String jsonText = response.getResultAsString();
                    Json json = new Json();
                    System.out.println(jsonText);
                    Gdx.app.log("InventoryService", "InventoryPull HTTP " + code + " → " + jsonText);

                    InventoryItemPayload[] arr = json.fromJson(InventoryItemPayload[].class, jsonText);
                    cb.onSuccess(Arrays.asList(arr));
                } catch (Exception e) {
                    cb.onFailure(e);
                }
            }

            @Override
            public void failed(Throwable t) {
                cb.onFailure(t);
            }

            @Override
            public void cancelled() {
                cb.onFailure(new RuntimeException("Request cancelled"));
            }
        });
    }

    /** Push an updated inventory list back to the server. */
    public static void pushInventory(
        List<InventoryItemPayload> items,
        Callback<Void> cb
    ) {
        // Wrap in the expected "inventory" field
        class PushRequest { List<InventoryItemPayload> inventory;
            PushRequest(List<InventoryItemPayload> inv){ inventory=inv; }
        }

        Json json = new Json();
        String body = json.toJson(new PushRequest(items));

        HttpRequest req = new HttpRequest(HttpMethods.POST);
        req.setUrl(BASE_URL + "/inventorypush");
        req.setHeader("Content-Type", "application/json");
        // req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());
        req.setContent(body);

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                // you could parse {"status":"OK"} here
                cb.onSuccess(null);
            }

            @Override
            public void failed(Throwable t) {
                cb.onFailure(t);
            }

            @Override
            public void cancelled() {
                cb.onFailure(new RuntimeException("Request cancelled"));
            }
        });
    }
}
