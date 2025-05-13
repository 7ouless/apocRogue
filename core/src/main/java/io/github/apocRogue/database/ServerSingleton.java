package io.github.apocRogue.database;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;

import java.sql.SQLException;

public final class ServerSingleton {
    private static ServerSingleton instance;
    private String authToken;
    public void setAuthToken(String t) { this.authToken = t; }

    private final Json json;
    private final String baseUrl;

    private ServerSingleton() {
        this.json    = new Json();
        this.baseUrl = "https://roetgeninstitute-48091364328.europe-west2.run.app";
    }

    /** thread-safe lazy init */
    public static synchronized ServerSingleton getInstance() {
        if (instance == null) {
            instance = new ServerSingleton();
        }
        return instance;
    }
    private void attachAuth(Net.HttpRequest req) {
        if (authToken != null) {
            req.setHeader("Authorization", "Bearer " + authToken);
        }
    }
    public String getAuthToken() { return authToken; }
    public String getBaseUrl() { return baseUrl; }
    public void fetchProfile(JsonCallback cb) {
        Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.GET);
        req.setUrl(baseUrl + "/profile");
        attachAuth(req);
        Gdx.net.sendHttpRequest(req, new DefaultListener(cb));
    }
    /** Shared response listener that just proxies to your callback */
    private class DefaultListener implements Net.HttpResponseListener {
        private final JsonCallback cb;
        DefaultListener(JsonCallback cb) { this.cb = cb; }

        @Override
        public void handleHttpResponse(Net.HttpResponse response) {
            cb.onSuccess(response.getResultAsString());
        }
        @Override
        public void failed(Throwable t) {
            cb.onError(t);
        }
        @Override public void cancelled() {}
    }

    public static void main(String[] args) {
        ServerSingleton instance = ServerSingleton.getInstance();

    }
}
