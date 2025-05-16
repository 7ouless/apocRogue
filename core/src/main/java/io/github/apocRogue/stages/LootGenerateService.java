package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import io.github.apocRogue.database.ServerSingleton;

public class LootGenerateService {
    public interface Callback<T> {

        void onSuccess(T value);

        void onFailure(Throwable t);
    }

    // Base URL for all Cloud Functions
    private static final String BASE =
        "https://europe-west2-studious-camp-458516-f5.cloudfunctions.net";

    private final Json json = new Json();

    //Request DTO
    public static class Req {
        public int difficulty;
        public int subLevel;
        public int radiation;
        public int count;
        public float chestX;
        public float chestY;
    }

    //Request DTO
    public static class Res {
        public String itemCode;
        // Use a concrete type so LibGDX Json can instantiate it
        public java.util.HashMap<String,Integer> stats;
    }

    public void generate(int difficulty, int subLevel, int radiation,
                         int count, float chestX, float chestY, Callback<Res[]> cb) {
        String url = BASE + "/generateloot";

        // Build JSON body
        Req body = new Req();
        body.difficulty = difficulty;
        body.subLevel   = subLevel;
        body.radiation  = radiation;
        body.count      = count;
        body.chestX = chestX;
        body.chestY = chestY;
        String jsonBody = json.toJson(body);

        // Prepare HTTP request
        Net.HttpRequest req = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " +
                ServerSingleton.getInstance().getAuthToken())
            .build();
        req.setContent(jsonBody);
        req.setTimeOut(10_000);

        // Send request with shared handler
        Gdx.net.sendHttpRequest(req, handler(cb, Res[].class));
    }

    private <T> Net.HttpResponseListener handler(Callback<T> cb, Class<T> typ) {
        return new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse resp) {
                int status = resp.getStatus().getStatusCode();
                String raw = resp.getResultAsString();
                Gdx.app.log("CHEST-RAW", raw);

                if (status != 200) {
                    cb.onFailure(new Exception("HTTP " + status + " — " + raw));
                    return;
                }

                try {
                    T parsed = json.fromJson(typ, raw);
                    cb.onSuccess(parsed);
                } catch (Exception ex) {
                    cb.onFailure(ex);
                }
            }
            @Override public void failed(Throwable t)    { cb.onFailure(t); }
            @Override public void cancelled()             { cb.onFailure(new Exception("cancelled")); }
        };
    }
}
