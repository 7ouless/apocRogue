package io.github.apocRogue.stages;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.apocRogue.userSettings.userSettingsManager;

public class AudioOverlay extends Window {
    private final OptionsOverlay previousOverlay;

    public AudioOverlay(Skin skin, OptionsOverlay previousOverlay) {
        // Window title, using Skin
        super("Audio Settings", skin);
        this.previousOverlay = previousOverlay;

        // Make the overlay fill the screen and block clicks behind it
        setModal(true);
        setFillParent(true);

        // A semi‐transparent black background
        setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));

        // Create a “content” table that we center within the full screen
        Table content = new Table();
        content.defaults().pad(10);
        // Place content table in the center
        add(content).expand().fill();

        // 1) Label row
        content.row();
        content.add(new Label("Master Volume", skin)).center();

        // 2) Volume slider row
        content.row();
        final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(userSettingsManager.getInstance().getMasterVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                userSettingsManager.getInstance().setMasterVolume(volumeSlider.getValue());
            }
        });
        content.add(volumeSlider).width(300).center();

        // 3) Back button row
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
        content.add(backButton).center();
    }


    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            // After we’re actually added to a stage, center ourselves:
            pack();
            float stageWidth  = stage.getViewport().getWorldWidth();
            float stageHeight = stage.getViewport().getWorldHeight();
            setPosition(
                (stageWidth - getWidth()) / 2f,
                (stageHeight - getHeight()) / 2f
            );
        }
    }
}
