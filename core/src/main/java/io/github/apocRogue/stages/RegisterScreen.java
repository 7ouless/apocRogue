package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class RegisterScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;

    public RegisterScreen(stageBuilder game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1080,720));
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Window win = new Window("Create Account", skin);
        win.defaults().pad(8);

        final TextField userField = new TextField("", skin);
        final TextField passField = new TextField("", skin);
        final TextField passConfirm = new TextField("", skin);
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        passConfirm.setPasswordMode(true);
        passConfirm.setPasswordCharacter('*');
        final Label feedback = new Label("", skin);

        win.add("Username:").row();
        win.add(userField).row();
        win.add("Password:").row();
        win.add(passField).row();
        win.add("Confirm:").row();
        win.add(passConfirm).row();
        win.add(feedback).row();

        TextButton createBtn = new TextButton("Register", skin);
        createBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String u = userField.getText();
                String p = passField.getText();
                String pc= passConfirm.getText();
                if (!p.equals(pc)) {
                    feedback.setText("Passwords do not match");
                    return;
                }
                try {
                    boolean done = DBManager.register(u, p);
                    if (done) {
                        game.setScreen(new MainScreen(game));
                    } else {
                        feedback.setText("Username taken");
                    }
                } catch (Exception e) {
                    feedback.setText("DB error");
                    e.printStackTrace();
                }
            }
        });

        TextButton backBtn = new TextButton("Back to Login", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LoginScreen(game));
            }
        });

        win.add(createBtn).padTop(16);
        win.add(backBtn).padTop(16);
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
