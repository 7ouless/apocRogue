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

    private static final String BASE_URL = "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    // Matches the JSON structure returned by InventoryPull
    public static class InventoryItemPayload {
        public String              itemCode;
        public String              typeID;
        public int                 skullLevel;
        public int                 skullSub;
        public HashMap<String,Integer> stats;
        public int                 count;
    }
    public static class CheckResponse {
        public boolean passed;
        public String message;
    }
    public static void checkInventory(
        List<InventoryItemPayload> items,
        Callback<CheckResponse> cb
    ) {
        class CheckRequest { List<InventoryItemPayload> inventory;
            CheckRequest(List<InventoryItemPayload> inv){ inventory = inv; }
        }
        Json json = new Json();
        String body = json.toJson(new CheckRequest(items));

        HttpRequest req = new HttpRequest(HttpMethods.POST);
        req.setUrl(BASE_URL + "/checkinventory");
        req.setHeader("Content-Type", "application/json");
        req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());
        req.setContent(body);

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override public void handleHttpResponse(HttpResponse response) {
                try {
                    int status = response.getStatus().getStatusCode();
                    String jsonText = response.getResultAsString();
                    if (status == 200) {
                        CheckResponse cr = new Json().fromJson(CheckResponse.class, jsonText);
                        cb.onSuccess(cr);
                    } else {
                        cb.onFailure(new RuntimeException("checkInventory HTTP " + status));
                    }
                } catch (Exception e) {
                    cb.onFailure(e);
                }
            }
            @Override public void failed(Throwable t) { cb.onFailure(t); }
            @Override public void cancelled()   { cb.onFailure(new RuntimeException("Request cancelled")); }
        });
    }

    // Fetch the full inventory for the current player
    public static void fetchInventory(Callback<List<InventoryItemPayload>> cb) {
        HttpRequest req = new HttpRequest(HttpMethods.GET);
        req.setUrl(BASE_URL + "/inventorypull");

        req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                try {
                    int code = response.getStatus().getStatusCode();

                    String jsonText = response.getResultAsString();
                    System.out.println(jsonText);
                    Gdx.app.log("InventoryService", "InventoryPull HTTP " + code + " → " + jsonText);
                    if (code != 200) {

                        cb.onFailure(new RuntimeException("InventoryPull HTTP " + code));
                        return;
                    }
                    Json json = new Json();

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

    // Push an updated inventory list back to the server
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
        req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());
        req.setContent(body);

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                int status = response.getStatus().getStatusCode();
                if (status == 200) {
                    cb.onSuccess(null);
                } else {
                    cb.onFailure(new RuntimeException(
                        "Inventory push failed: HTTP " + status + " – " + response.getResultAsString()
                    ));
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
    public static void wipeInventory(
        List<InventoryItemPayload> items,
        Callback<Void> cb
    ) {

        class WipeRequest { List<InventoryItemPayload> inventory;
            WipeRequest(List<InventoryItemPayload> inv){ inventory = inv; }
        }

        Json json = new Json();
        String body = json.toJson(new WipeRequest(items));

        HttpRequest req = new HttpRequest(HttpMethods.POST);
        req.setUrl(BASE_URL + "/inventorywipe");
        req.setHeader("Content-Type", "application/json");
        req.setHeader("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken());
        req.setContent(body);

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override public void handleHttpResponse(HttpResponse response) {
                if (response.getStatus().getStatusCode() == 200) {
                    cb.onSuccess(null);
                } else {
                    cb.onFailure(new RuntimeException("Wipe HTTP "
                        + response.getStatus().getStatusCode()));
                }
            }
            @Override public void failed(Throwable t) { cb.onFailure(t); }
            @Override public void cancelled()   { cb.onFailure(
                new RuntimeException("Wipe cancelled")); }
        });

    }


}
