package io.github.apocRogue.OMarket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.google.gson.*;
import io.github.apocRogue.database.ServerSingleton;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MarketService {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
                return Instant.parse(json.getAsString());
            }
        })
        .create();

    public interface Callback<T> {
        void onSuccess(T value);
        void onFailure(Throwable t);
    }

    private static final String BASE =
        "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    private static final Json json = new Json();


    public static void pullListings(int page,
                                    int size,
                                    String itemCode,
                                    Long minPrice,
                                    Long maxPrice,
                                    String sortBy,
                                    Callback<List<MarketListing>> callback) {
        Gdx.app.log("MarketService", "pullListings() start: page=" + page + ", size=" + size + ", itemCode=" + itemCode);
        String url = BASE + "/marketpull";

        // build a simple map
        Map<String, Object> body = new HashMap<>();
        body.put("page", page);
        body.put("size", size);

        if (itemCode != null) body.put("itemCode", itemCode);
        if (minPrice != null) body.put("minPrice", minPrice);
        if (maxPrice != null) body.put("maxPrice", maxPrice);
        if (sortBy != null)  body.put("sortBy", sortBy);

        // use Gson instead of libGDX Json
        String payload = GSON.toJson(body);
        Gdx.app.log("MarketService", "pullListings payload: " + payload);


        try {
            Net.HttpRequest request = new HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.POST)
                .url(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
                .content(payload)
                .build();

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    int status = httpResponse.getStatus().getStatusCode();
                    String responseText = httpResponse.getResultAsString();
                    Gdx.app.log("MarketService",
                        "pullListings HTTP " + status + ": " + responseText);

                    if (status < 200 || status >= 300) {
                        callback.onFailure(new RuntimeException("HTTP " + status + ": " + responseText));
                        return;
                    }

                    try {
                        // 1) JSON array parse
                        MarketListing[] arr = GSON.fromJson(responseText, MarketListing[].class);
                        List<MarketListing> list = (arr != null)
                            ? Arrays.asList(arr)
                            : Collections.emptyList();
                        Gdx.app.log("MarketService", "Parsed " + list.size() + " listings");
                        callback.onSuccess(list);

                    } catch (JsonSyntaxException e) {
                        // Malformed JSON structure (e.g. BEGIN_OBJECT when expecting BEGIN_ARRAY)
                        Gdx.app.error("MarketService", "Malformed JSON, returning empty", e);
                        callback.onSuccess(Collections.emptyList());

                    } catch (JsonIOException e) {
                        // I/O problem during JSON read/write
                        Gdx.app.error("MarketService", "JSON I/O error parsing listings", e);
                        callback.onFailure(e);

                    } catch (IllegalStateException e) {
                        // Unexpected state in JSON reader or GSON internals
                        Gdx.app.error("MarketService", "Illegal state while parsing listings", e);
                        callback.onFailure(e);

                    } catch (IllegalArgumentException e) {
                        // Bad arguments passed to GSON adapter
                        Gdx.app.error("MarketService", "Invalid argument in JSON parsing", e);
                        callback.onFailure(e);

                    } catch (Exception e) {
                        // Catch anything else (e.g. NPEs, reflection issues, etc.)
                        Gdx.app.error("MarketService", "Unexpected error handling pullListings response", e);
                        callback.onFailure(e);
                    }
                }

                @Override
                public void failed(Throwable throwable) {

                }

                @Override
                public void cancelled() {

                }
            });
        } catch (Exception e) {
            Gdx.app.error("MarketService", "Exception in pullListings setup", e);
            callback.onFailure(e);
        }
    }


    public static void sellItem(String itemCode,
                                long price,
                                Callback<Void> callback) {
        Gdx.app.log("MarketService", "sellItem() start: itemCode=" + itemCode + ", price=" + price);
        String url = BASE + "/marketsell";
        Map<String, Object> body = new HashMap<>();
        body.put("itemCode", itemCode);
        body.put("price", price);
        String payload = "{"
            + "\"itemCode\":\"" + itemCode + "\","
            + "\"price\":"    + price
            + "}";
        Gdx.app.log("MarketService", "sellItem payload: " + payload);

        try {
            Net.HttpRequest request = new HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.POST)
                .url(url)
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
                .content(payload)
                .build();

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    Gdx.app.log("MarketService", "sellItem HTTP response received");
                    System.out.println(httpResponse.getResultAsString());
                    callback.onSuccess(null);
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("MarketService", "sellItem HTTP request failed", t);
                    callback.onFailure(t);
                }

                @Override
                public void cancelled() {
                    Gdx.app.log("MarketService", "sellItem HTTP request cancelled");
                    callback.onFailure(new RuntimeException("Request cancelled"));
                }
            });
        } catch (Exception e) {
            Gdx.app.error("MarketService", "Exception in sellItem setup", e);
            callback.onFailure(e);
        }
    }


    public static void buyItem(long listingId,
                               Callback<Void> callback) {
        Gdx.app.log("MarketService", "buyItem() start: listingId=" + listingId);
        String url = BASE + "/marketbuy";

        // manually build a flat JSON payload
        String payload = Long.toString(listingId);
        Gdx.app.log("MarketService", "buyItem payload: " + payload);

        try {
            Net.HttpRequest request = new HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.POST)
                .url(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
                .content(payload)
                .build();

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    Gdx.app.log("MarketService", "buyItem HTTP response received");
                    callback.onSuccess(null);
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("MarketService", "buyItem HTTP request failed", t);
                    callback.onFailure(t);
                }

                @Override
                public void cancelled() {
                    Gdx.app.log("MarketService", "buyItem HTTP request cancelled");
                    callback.onFailure(new RuntimeException("Request cancelled"));
                }
            });
        } catch (Exception e) {
            Gdx.app.error("MarketService", "Exception in buyItem setup", e);
            callback.onFailure(e);
        }
    }
}
