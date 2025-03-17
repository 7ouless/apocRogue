package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.apocRogue.userSettings.userSettingsManager;

public class ControlsScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private stageBuilder game;

    // UI labels for current bindings
    private Label moveLeftLabel;
    private Label moveRightLabel;
    private Label jumpLabel;
    private Label dashLeftLabel;
    private Label dashRightLabel;
    private Label interactLabel;

    public ControlsScreen(stageBuilder game, GameScreen gameScreen) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Initialize labels with current settings
        updateLabels();

        // Row for "Move Left"
        table.add(new Label("Move Left:", skin)).pad(10);
        table.add(moveLeftLabel).pad(10);
        TextButton rebindMoveLeft = new TextButton("Rebind", skin);
        rebindMoveLeft.addListener(new RebindListener("moveLeft"));
        table.add(rebindMoveLeft).pad(10);
        table.row();

        // Row for "Move Right"
        table.add(new Label("Move Right:", skin)).pad(10);
        table.add(moveRightLabel).pad(10);
        TextButton rebindMoveRight = new TextButton("Rebind", skin);
        rebindMoveRight.addListener(new RebindListener("moveRight"));
        table.add(rebindMoveRight).pad(10);
        table.row();

        // Row for "Jump"
        table.add(new Label("Jump:", skin)).pad(10);
        table.add(jumpLabel).pad(10);
        TextButton rebindJump = new TextButton("Rebind", skin);
        rebindJump.addListener(new RebindListener("jump"));
        table.add(rebindJump).pad(10);
        table.row();

        // Row for "Dash Left"
        table.add(new Label("Dash Left:", skin)).pad(10);
        table.add(dashLeftLabel).pad(10);
        TextButton rebindDashLeft = new TextButton("Rebind", skin);
        rebindDashLeft.addListener(new RebindListener("dashLeft"));
        table.add(rebindDashLeft).pad(10);
        table.row();

        // Row for "Dash Right"
        table.add(new Label("Dash Right:", skin)).pad(10);
        table.add(dashRightLabel).pad(10);
        TextButton rebindDashRight = new TextButton("Rebind", skin);
        rebindDashRight.addListener(new RebindListener("dashRight"));
        table.add(rebindDashRight).pad(10);
        table.row();

        // Row for "Interact"
        table.add(new Label("Interact:", skin)).pad(10);
        table.add(interactLabel).pad(10);
        TextButton rebindInteract = new TextButton("Rebind", skin);
        rebindInteract.addListener(new RebindListener("interact"));
        table.add(rebindInteract).pad(10);
        table.row();

        // Back button to return to OptionsScreen
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new OptionsScreen(game, gameScreen));
            }
        });
        table.add(backButton).colspan(3).pad(10);
    }

    // Named inner class for handling rebind events.
    private class RebindListener extends ChangeListener {
        private String control;

        public RebindListener(String control) {
            this.control = control;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            promptRebind(control);
        }
    }

    // Update the labels with the current key names from the settings manager.
    private void updateLabels() {
        userSettingsManager settings = userSettingsManager.getInstance();
        moveLeftLabel = new Label(Input.Keys.toString(settings.getMoveLeft()), skin);
        moveRightLabel = new Label(Input.Keys.toString(settings.getMoveRight()), skin);
        jumpLabel = new Label(Input.Keys.toString(settings.getJump()), skin);
        dashLeftLabel = new Label(Input.Keys.toString(settings.getDashLeft()), skin);
        dashRightLabel = new Label(Input.Keys.toString(settings.getDashRight()), skin);
        interactLabel = new Label(Input.Keys.toString(settings.getInteract()), skin);
    }

    // Prompt the user to press a new key for the given control.
    private void promptRebind(final String control) {
        final Dialog dialog = new Dialog("Rebind " + control, skin) {
            protected void result(Object object) {
                // Handle confirmation if needed.
            }
        };
        dialog.text("Press a new key for " + control);
        dialog.button("Cancel", -1);
        dialog.show(stage);

        // Temporarily override the input processor to capture the next key press.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                userSettingsManager settings = userSettingsManager.getInstance();
                if (control.equals("moveLeft")) {
                    settings.setMoveLeft(keycode);
                } else if (control.equals("moveRight")) {
                    settings.setMoveRight(keycode);
                } else if (control.equals("jump")) {
                    settings.setJump(keycode);
                } else if (control.equals("dashLeft")) {
                    settings.setDashLeft(keycode);
                } else if (control.equals("dashRight")) {
                    settings.setDashRight(keycode);
                } else if (control.equals("interact")) {
                    settings.setInteract(keycode);
                }
                dialog.hide();
                updateLabels(); // Refresh labels to reflect the new key.
                Gdx.input.setInputProcessor(stage);
                return true;
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
