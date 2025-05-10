package io.github.apocRogue.database;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.google.gson.Gson;
import io.github.apocRogue.database.JsonCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {
    private static DBManager instance;
    private final Json json = new Json();
    {json.setOutputType(JsonWriter.OutputType.minimal);}
    private static final Gson GSON = new Gson();

    private final String baseUrl =
        "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    private DBManager() {
    }

    public static DBManager get() {
        if (instance == null) instance = new DBManager();
        return instance;
    }

    /**
     * Registers a new user; cb.onSuccess receives the raw JSON response.
     */
    public void register(String user, String pass, JsonCallback cb) {
        System.out.println("Registering user " + user + " with password " + pass);
        post("/registrationSystem", user, pass, cb);
        System.out.println("Registration finished");
    }

    /**
     * Logs in an existing user; cb.onSuccess receives the raw JSON response.
     */
    public void login(String user, String pass, JsonCallback cb) {
        System.out.println("Login called");
        post("/loginSystem", user, pass, cb);
        System.out.println("Login finished");
    }

    private static final String HEALTH_URL =
        "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net/healthCheckFunction";

    /**
     * Performs a health check; cb.onSuccess receives the raw response body.
     */
    public void healthCheck(final JsonCallback cb) {
        Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.GET);
        req.setUrl(HEALTH_URL);
        req.setTimeOut(10_000);
        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                cb.onSuccess(response);
            }

            @Override
            public void failed(Throwable t) {
                cb.onError(t);
            }

            @Override
            public void cancelled() {
                cb.onError(new RuntimeException("Request cancelled"));
            }
        });
    }

    private void post(String path, String user, String pass, final JsonCallback cb) {
        String url = baseUrl + path;
        System.out.println("Posting user " + user + " with password " + pass);
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", user);
        bodyMap.put("password", pass);
        String body = GSON.toJson(bodyMap);

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .build();
        req.setContent(body);
        req.setTimeOut(10_000);    // <<< give it 10 seconds to connect/read

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse resp) {
                int status = resp.getStatus().getStatusCode();
                String body = resp.getResultAsString();
                System.out.println("HTTP " + status + " → " + body);

                // 1) Success path
                if (status >= 200 && status < 300) {
                    try {
                        JsonValue parsed = new JsonReader().parse(body);
                        cb.onSuccess(parsed);
                    } catch (Exception e) {
                        cb.onError(new RuntimeException("Invalid JSON in success response", e));
                    }
                    return;
                }

                // 2) Error path
                String message;
                try {
                    // if you wrap errors as JSON: {"error":"…"}
                    message = new JsonReader().parse(body).getString("error");
                } catch (Exception e) {
                    // fallback to raw body
                    message = body;
                }
                cb.onError(new RuntimeException("HTTP " + status + ": " + message));
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();               // full stack trace
                System.out.println(t.toString());  // class + message
                cb.onError(t);
            }

            @Override
            public void cancelled() {
                System.out.println("Request cancelled");
                cb.onError(new Exception("Request cancelled"));
            }
        });
    }
    public void fetchMarket(final JsonCallback cb) {
        String url = baseUrl + "/marketPull";
        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            // attach the auth token from your ServerSingleton
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();

        // send an empty JSON object as body (no filters)
        req.setContent("{}");
        req.setTimeOut(10_000);

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                int status = response.getStatus().getStatusCode();
                String body   = response.getResultAsString();
                if (status >= 200 && status < 300) {
                    try {
                        JsonValue parsed = new JsonReader().parse(body);
                        cb.onSuccess(parsed);
                    } catch (Exception e) {
                        cb.onError(new RuntimeException("Invalid JSON in success response", e));
                    }
                } else {
                    String msg;
                    try {
                        msg = new JsonReader().parse(body).getString("error");
                    } catch (Exception e) {
                        msg = body;
                    }
                    cb.onError(new RuntimeException("HTTP " + status + ": " + msg));
                }
            }

            @Override public void failed(Throwable t)    { cb.onError(t); }
            @Override public void cancelled()             { cb.onError(new Exception("Request cancelled")); }
        });
    }
    public void sell(String itemCode, long price, JsonCallback cb) {
        String url = baseUrl + "/marketSell";
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("itemCode", itemCode);
        bodyMap.put("price",    price);
        String body = GSON.toJson(bodyMap);

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setContent(body);
        req.setTimeOut(10_000);

        Gdx.net.sendHttpRequest(req, new DefaultListener(cb));
    }

    /**
     * Buy the given listingID.  Expects { listingID } and returns { status:"PURCHASED" }.
     */
    public void buy(long listingID, JsonCallback cb) {
        String url = baseUrl + "/marketBuy";
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("listingID", listingID);
        String body = GSON.toJson(bodyMap);

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setContent(body);
        req.setTimeOut(10_000);

        Gdx.net.sendHttpRequest(req, new DefaultListener(cb));
    }

    /** shared listener that handles 2xx vs errors, exactly like your other methods */
    private static class DefaultListener implements Net.HttpResponseListener {
        private final JsonCallback cb;
        DefaultListener(JsonCallback cb) { this.cb = cb; }

        @Override
        public void handleHttpResponse(Net.HttpResponse resp) {
            int status = resp.getStatus().getStatusCode();
            String body = resp.getResultAsString();
            if (status >= 200 && status < 300) {
                try {
                    JsonValue parsed = new JsonReader().parse(body);
                    cb.onSuccess(parsed);
                } catch (Exception e) {
                    cb.onError(new RuntimeException("Invalid JSON in success response", e));
                }
            } else {
                String msg;
                try {
                    msg = new JsonReader().parse(body).getString("error");
                } catch (Exception e) {
                    msg = body;
                }
                cb.onError(new RuntimeException("HTTP " + status + ": " + msg));
            }
        }
        @Override public void failed(Throwable t)    { cb.onError(t); }
        @Override public void cancelled()             { cb.onError(new Exception("Request cancelled")); }
    }
    public void pushInventory(Map<String,Integer> items, JsonCallback cb) {
        String url = baseUrl + "/inventoryPush";
        List<Map<String,Object>> list = new ArrayList<>();
        items.forEach((code,count) -> {
            list.add(Map.of("itemCode", code, "count", count));
        });
        Map<String,Object> body = Map.of("inventory", list);
        String json = GSON.toJson(body);

        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type","application/json")
            .header("Authorization","Bearer "+ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setContent(json);
        Gdx.net.sendHttpRequest(req, new DefaultListener(cb));
    }
    public void fetchInventory(final JsonCallback cb) {
        String url = baseUrl + "/inventoryPull";
        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ServerSingleton.getInstance().getAuthToken())
            .build();

        // Empty JSON body (no additional filters)
        req.setContent("{}");
        req.setTimeOut(10_000);

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                int status = response.getStatus().getStatusCode();
                String body = response.getResultAsString();

                if (status >= 200 && status < 300) {
                    try {
                        JsonValue parsed = new JsonReader().parse(body);
                        cb.onSuccess(parsed);               // hands you JsonValue array
                    } catch (Exception e) {
                        cb.onError(new RuntimeException("Invalid JSON in success response", e));
                    }
                } else {
                    // pull out {"error":"…"} or fallback
                    String msg;
                    try {
                        msg = new JsonReader().parse(body).getString("error");
                    } catch (Exception e) {
                        msg = body;
                    }
                    cb.onError(new RuntimeException("HTTP " + status + ": " + msg));
                }
            }

            @Override
            public void failed(Throwable t) {
                cb.onError(t);
            }

            @Override
            public void cancelled() {
                cb.onError(new RuntimeException("Request cancelled"));
            }
        });
    }

}
