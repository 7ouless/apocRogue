package io.github.apocRogue.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.apocRogue.userSettings.userSettingsManager;

public class ControlsOverlay extends Window {

    private final Skin skin;
    private final OptionsOverlay previousOverlay;

    // Labels for current bindings
    private Label moveLeftLabel;
    private Label moveRightLabel;
    private Label jumpLabel;
    private Label dashLeftLabel;
    private Label dashRightLabel;
    private Label interactLabel;

    public ControlsOverlay(Skin skin, OptionsOverlay previousOverlay) {
        super("Controls", skin);
        this.skin = skin;
        this.previousOverlay = previousOverlay;

        // Fill entire screen, block clicks behind
        setModal(true);
        setFillParent(true);
        setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));

        // Create a content table for the controls UI
        Table content = new Table();
        content.defaults().pad(10);
        add(content).expand().fill();

        // Build the control labels & rebind buttons
        updateLabels();

        // 1) Move Left row
        content.row();
        content.add(new Label("Move Left:", skin));
        content.add(moveLeftLabel);
        TextButton rebindMoveLeft = new TextButton("Rebind", skin);
        rebindMoveLeft.addListener(new RebindListener("moveLeft"));
        content.add(rebindMoveLeft);

        // 2) Move Right
        content.row();
        content.add(new Label("Move Right:", skin));
        content.add(moveRightLabel);
        TextButton rebindMoveRight = new TextButton("Rebind", skin);
        rebindMoveRight.addListener(new RebindListener("moveRight"));
        content.add(rebindMoveRight);

        // 3) Jump
        content.row();
        content.add(new Label("Jump:", skin));
        content.add(jumpLabel);
        TextButton rebindJump = new TextButton("Rebind", skin);
        rebindJump.addListener(new RebindListener("jump"));
        content.add(rebindJump);

        // 4) Dash Left
        content.row();
        content.add(new Label("Dash Left:", skin));
        content.add(dashLeftLabel);
        TextButton rebindDashLeft = new TextButton("Rebind", skin);
        rebindDashLeft.addListener(new RebindListener("dashLeft"));
        content.add(rebindDashLeft);

        // 5) Dash Right
        content.row();
        content.add(new Label("Dash Right:", skin));
        content.add(dashRightLabel);
        TextButton rebindDashRight = new TextButton("Rebind", skin);
        rebindDashRight.addListener(new RebindListener("dashRight"));
        content.add(rebindDashRight);

        // 6) Interact
        content.row();
        content.add(new Label("Interact:", skin));
        content.add(interactLabel);
        TextButton rebindInteract = new TextButton("Rebind", skin);
        rebindInteract.addListener(new RebindListener("interact"));
        content.add(rebindInteract);

        // 7) Back button row
        content.row();
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                remove();
                if (previousOverlay != null) {

                    Stage currentStage = getStage();
                    if (currentStage != null) {
                        currentStage.addActor(previousOverlay);
                    }
                }
            }
        });
    }

    private void updateLabels() {
        userSettingsManager settings = userSettingsManager.getInstance();
        moveLeftLabel  = new Label(Input.Keys.toString(settings.getMoveLeft()),  skin);
        moveRightLabel = new Label(Input.Keys.toString(settings.getMoveRight()), skin);
        jumpLabel      = new Label(Input.Keys.toString(settings.getJump()),      skin);
        dashLeftLabel  = new Label(Input.Keys.toString(settings.getDashLeft()),  skin);
        dashRightLabel = new Label(Input.Keys.toString(settings.getDashRight()), skin);
        interactLabel  = new Label(Input.Keys.toString(settings.getInteract()),  skin);
    }

    private class RebindListener extends ChangeListener {
        private final String control;
        public RebindListener(String control) {
            this.control = control;
        }
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            promptRebind(control);
        }
    }

    private void promptRebind(final String control) {
        final Dialog dialog = new Dialog("Rebind " + control, skin) {
            protected void result(Object object) {}
        };
        dialog.text("Press a new key for " + control);
        dialog.button("Cancel", -1);
        dialog.show(getStage()); // show on the same stage as this overlay

        // Temporarily override the input processor to capture the key press
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                userSettingsManager settings = userSettingsManager.getInstance();
                switch (control) {
                    case "moveLeft":  settings.setMoveLeft(keycode);  break;
                    case "moveRight": settings.setMoveRight(keycode); break;
                    case "jump":      settings.setJump(keycode);      break;
                    case "dashLeft":  settings.setDashLeft(keycode);  break;
                    case "dashRight": settings.setDashRight(keycode); break;
                    case "interact":  settings.setInteract(keycode);  break;
                }
                dialog.hide();
                updateLabels();
                // Restore original input
                Gdx.input.setInputProcessor(getStage());
                return true;
            }
        });
    }
}
