package io.github.apocRogue.database;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;

import java.sql.SQLException;

public final class ServerSingleton {
    private static ServerSingleton instance;

    private final HttpRequestBuilder builder;
    private final Json json;
    private final String baseUrl;

    private ServerSingleton() {
        this.builder = new HttpRequestBuilder();
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
