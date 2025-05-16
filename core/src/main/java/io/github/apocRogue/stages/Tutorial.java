package io.github.apocRogue.stages;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

public class Tutorial extends Dialog {
    private final Label contentLabel;
    private final String fullText =
            "Welcome to Kigen!\n\n" +
                "• Deploy, loot, survive, escape.\n" +
                "• Stockpile gear to reach the [REDACTED].\n" +
                "• Items equipped in the inventory will follow you into the loop.\n" +
                "• Lose everything you took with you if... [ERROR] when you die.\n" +
                "• Try again next cycle.\n\n" +

                "Good luck, [REDACTED]!";

    public Tutorial(Skin skin) {
        super("", skin);
        // Remove default title table
        getTitleTable().clear();

        // Padding
        getContentTable().pad(15);
        getButtonTable().padTop(10).padBottom(10);

        // Create content label
        contentLabel = new Label("", skin);
        contentLabel.setAlignment(Align.topLeft);
        contentLabel.setWrap(true);

        // Wrap label in a scroll pane
        ScrollPane scrollPane = new ScrollPane(contentLabel, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);

        // Add scroll pane to content table with predefined size
        getContentTable().add(scrollPane).size(400, 200);

        // Add close button
        button("Close");

    }

    @Override
    public Dialog show(Stage stage) {
        super.show(stage);

        setSize(600, 400);

        // Center dialog on screen
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();
        setPosition(
            (vw - getWidth()) / 2f,
            (vh - getHeight()) / 2f
        );

        // Typewriter effect
        Timer.schedule(new Timer.Task() {
            int idx = 0;
            @Override
            public void run() {
                if (idx < fullText.length()) {
                    contentLabel.setText(contentLabel.getText().toString() + fullText.charAt(idx++));
                } else {
                    cancel();
                }
            }
        }, 0f, 0.05f);

        return this;
    }

    @Override
    protected void result(Object object) {
        // Hide dialog when a button is clicked
        hide();
    }
}
