package io.github.apocRogue.stages;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class OptionsOverlay extends Window {

    public interface OptionsListener {
        void onShowControls();
        void onShowAudio();
        void onCloseOptions();
    }
    private OptionsListener listener; // starts null

    public OptionsOverlay(Skin skin) {
        super("Options", skin);

        setModal(true);
        defaults().pad(30).center();
        row();
        TextButton controlsButton = new TextButton("Controls", skin);
        add(controlsButton).row();

        TextButton audioButton = new TextButton("Audio", skin);
        add(audioButton).row();

        TextButton backButton = new TextButton("Back", skin);
        add(backButton).row();

        pack(); //shrink-wrap the Window around its contents
        if (getStage() != null) {
             setPosition(
                 (getStage().getWidth() - getWidth()) / 2f,
                 (getStage().getHeight() - getHeight()) / 2f
             );
        }
        controlsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onShowControls();
            }
        });

        audioButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onShowAudio();
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onCloseOptions();
            }
        });

    }
    public void setOptionsListener(OptionsListener listener) {
        this.listener = listener;
    }
}
