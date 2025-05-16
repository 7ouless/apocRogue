package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;
import io.github.apocRogue.database.ServerSingleton;

public class LoginScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;

    public LoginScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(400, 280));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table(skin);
        table.setFillParent(true);
        table.pad(15);

        // Title
        Label title = new Label("Welcome to Kigen", skin);
        title.setFontScale(1.3f);
        table.add(title).colspan(2).padBottom(15f).row();

        // Username
        table.add(new Label("Username:", skin)).left().padBottom(8f);
        TextField userField = new TextField("", skin);
        userField.getStyle().background =
            skin.newDrawable("textfield", 0.9f, 0.9f, 0.9f, 1f);
        userField.setAlignment(Align.center);
        table.add(userField).width(180f).padBottom(8f).row();

        // Password
        table.add(new Label("Password:", skin)).left().padBottom(8f);
        TextField passField = new TextField("", skin);
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        passField.getStyle().background =
            skin.newDrawable("textfield", 0.9f, 0.9f, 0.9f, 1f);
        passField.setAlignment(Align.center);
        table.add(passField).width(180f).padBottom(10f).row();

        // Feedback
        Label feedback = new Label("", skin);
        table.add(feedback).colspan(2).padBottom(10f).row();

        // Buttons
        TextButton loginBtn    = new TextButton("Login",         skin);
        TextButton registerBtn = new TextButton("Create Account", skin);
        loginBtn.pad(6f,12f,6f,12f);
        registerBtn.pad(6f,12f,6f,12f);

        loginBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                String user = userField.getText().trim();
                String pass = passField.getText().trim();
                feedback.setText("…loading…");

                DBManager.get().login(user, pass, new JsonCallback() {
                    @Override public void onSuccess(String json) {
                        JsonValue data = new JsonReader().parse(json);
                        onSuccess(data);
                    }

                    @Override public void onSuccess(JsonValue data) {
                        boolean ok = data.getBoolean("authenticated", false);
                        if (!ok) {
                            Gdx.app.postRunnable(() -> feedback.setText("Bad username or password"));
                            return;
                        }
                        // Extract and store the token
                        String token = data.getString("token");
                        ServerSingleton.getInstance().setAuthToken(token);
                        // Proceed to main screen
                        Gdx.app.postRunnable(() -> game.setScreen(new MainScreen(game)));
                    }

                    @Override public void onError(Throwable t) {
                        Gdx.app.postRunnable(() -> feedback.setText("Network error: " + t.getMessage()));
                    }
                });
            }
        });

        registerBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new RegisterScreen(game));
            }
        });

        table.add(loginBtn).width(100f).padRight(8f);
        table.add(registerBtn).width(100f).row();

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f,0.1f,0.1f,1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
