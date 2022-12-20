package com.igrium.replayeditorplus.ui;

import com.igrium.craftfx.util.ThreadUtils;
import com.igrium.replayeditorplus.ReplayEditor;
import com.igrium.replayeditorplus.ui.controls.TimelineUI;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class TimelineWindow {

    @FXML
    private Slider speedSlider;

    @FXML
    private TimelineUI timeline;

    private ReplayEditor editor;

    @FXML
    protected void initialize() {
        timeline.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                ThreadUtils.onRenderThread(() -> {
                    editor.getReplayHandler().doJump((int) (timeline.getTimeAt(event.getX()) * 1000), true);
                });
                event.consume();
            }
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (editor.replayProperties().isPaused()) return;
            editor.setReplayPlayback(newVal.doubleValue());
        });
    }

    @FXML
    public void onPlayPause() {
        boolean paused = editor.replayProperties().isPaused();
        if (paused) {
            editor.setReplayPlayback(speedSlider.getValue());
        } else {
            editor.setReplayPlayback(0);
        }
    }

    public void initEditor(ReplayEditor editor) {
        this.editor = editor;
        timeline.timeProperty().bind(editor.replayProperties().gameTimestamp());
        timeline.endProperty().bind(editor.replayProperties().replayDuration());
    }

    public ReplayEditor getEditor() {
        return editor;
    }
}
