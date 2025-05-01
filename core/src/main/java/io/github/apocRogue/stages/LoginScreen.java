package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;

public class LoginScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;

    public LoginScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1080,720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Window win = new Window("Login", skin);
        win.defaults().pad(8);

        final TextField userField = new TextField("", skin);
        final TextField passField = new TextField("", skin);
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        final Label feedback = new Label("", skin);

        win.add("Username:").row();
        win.add(userField).row();
        win.add("Password:").row();
        win.add(passField).row();
        win.add(feedback).row();

        TextButton loginBtn = new TextButton("Login", skin);
        loginBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    boolean ok = DBManager.authenticate(userField.getText(), passField.getText());
                    if (ok) {
                        game.setScreen(new MainScreen(game));
                    } else {
                        feedback.setText("Invalid credentials");
                    }
                } catch (Exception e) {
                    feedback.setText("DB error");
                    e.printStackTrace();
                }
            }
        });
        TextButton registerBtn = new TextButton("Create Account", skin);
        registerBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new RegisterScreen(game));
            }
        });

        win.add(loginBtn).padTop(16);
        win.add(registerBtn).padTop(16);
        win.pack();
        win.setPosition(
            stage.getWidth()/2 - win.getWidth()/2,
            stage.getHeight()/2 - win.getHeight()/2
        );
        stage.addActor(win);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
