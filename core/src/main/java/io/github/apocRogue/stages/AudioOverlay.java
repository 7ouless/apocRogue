package io.github.apocRogue.stages;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.apocRogue.userSettings.userSettingsManager;

public class AudioOverlay extends Window {
    private final OptionsOverlay previousOverlay;

    public AudioOverlay(Skin skin, OptionsOverlay previousOverlay) {
        //window title, same skin
        super("Audio Settings", skin);
        this.previousOverlay = previousOverlay;

        //overlay fill screen
        setModal(true);
        setFillParent(true);

        //black backround
        setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));

        //creating and centering a 'content table'
        Table content = new Table();
        content.defaults().pad(10);
        add(content).expand().fill();

        content.row();
        content.add(new Label("Master Volume", skin)).center();

        //volume slider
        content.row();
        final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(userSettingsManager.getInstance().getMasterVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Update the user settings volume
                userSettingsManager.getInstance().setMasterVolume(volumeSlider.getValue());
            }
        });
        content.add(volumeSlider).width(300).center();

        //back button
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
