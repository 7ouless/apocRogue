package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.apocRogue.database.DBManager;

public class RegisterScreen extends ScreenAdapter {
    private final stageBuilder game;
    private Stage stage;
    private Skin skin;

    public RegisterScreen(stageBuilder game) {
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
        Label title = new Label("Create Account", skin);
        title.setFontScale(1.3f);
        table.add(title).colspan(2).padBottom(15f).row();

        // Username label + field
        table.add(new Label("Username:", skin)).left().padBottom(8f);
        TextField userField = new TextField("", skin);
        userField.getStyle().background =
            skin.newDrawable("textfield", 0.9f, 0.9f, 0.9f, 1f);
        userField.setAlignment(Align.center);
        table.add(userField).width(180f).padBottom(8f).row();

        // Password label + field
        table.add(new Label("Password:", skin)).left().padBottom(8f);
        TextField passField = new TextField("", skin);
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        passField.getStyle().background =
            skin.newDrawable("textfield", 0.9f, 0.9f, 0.9f, 1f);
        passField.setAlignment(Align.center);
        table.add(passField).width(180f).padBottom(8f).row();

        // Confirm label + field
        table.add(new Label("Confirm:", skin)).left().padBottom(8f);
        TextField passConfirm = new TextField("", skin);
        passConfirm.setPasswordMode(true);
        passConfirm.setPasswordCharacter('*');
        passConfirm.getStyle().background =
            skin.newDrawable("textfield", 0.9f, 0.9f, 0.9f, 1f);
        passConfirm.setAlignment(Align.center);
        table.add(passConfirm).width(180f).padBottom(10f).row();

        // Feedback
        Label feedback = new Label("", skin);
        table.add(feedback).colspan(2).padBottom(10f).row();

        // Buttons
        TextButton createBtn = new TextButton("Register",     skin);
        TextButton backBtn   = new TextButton("Back to Login", skin);
        createBtn.pad(6f,12f,6f,12f);
        backBtn.pad(6f,12f,6f,12f);

        createBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                String u = userField.getText();
                String p = passField.getText();
                String pc= passConfirm.getText();
                if (!p.equals(pc)) {
                    feedback.setText("Passwords do not match");
                    return;
                }
                if (DBManager.register(u, p)) {
                    game.setScreen(new MainScreen(game));
                } else {
                    feedback.setText("Username taken");
                }
            }
        });
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new LoginScreen(game));
            }
        });

        table.add(createBtn).width(100f).padRight(8f);
        table.add(backBtn).width(100f).row();

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
