package com.igrium.replayeditorplus.ui;

import com.igrium.craftfx.util.ThreadUtils;
import com.igrium.replayeditorplus.ReplayEditor;
import com.igrium.replayeditorplus.ui.controls.TimelineUI;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class TimelineWindow {

    @FXML
    private Slider speedSlider;

    @FXML
    private ScrollPane timelinePane;

    @FXML
    private ToggleButton directPlaybackButton;

    private TimelineUI directTimeline;
    private TimelineUI replayTimeline;

    private ReplayEditor editor;

    private ObjectBinding<TimelineUI> timelineBinding;

    @FXML
    protected void initialize() {
        directTimeline = new TimelineUI();
        directTimeline.setPrefScale(32);
        timelinePane.setContent(directTimeline);

        replayTimeline = new TimelineUI();
        replayTimeline.setPrefScale(64);

        directTimeline.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                ThreadUtils.onRenderThread(() -> {
                    editor.getReplayHandler().doJump((int) (directTimeline.getTimeAt(event.getX()) * 1000), true);
                });
                event.consume();
            }
        });

        timelineBinding = new ObjectBinding<>() {
            {
                super.bind(directPlaybackProperty());
            }

            @Override
            protected TimelineUI computeValue() {
                return directPlaybackProperty().get() ? directTimeline : replayTimeline;
            }
        };

        timelinePane.contentProperty().bind(timelineBinding);
    }

    @FXML
    public void onPlayPause() {
        editor.togglePause();
    }

    public BooleanProperty directPlaybackProperty() {
        return directPlaybackButton.selectedProperty();
    }

    public void initEditor(ReplayEditor editor) {
        this.editor = editor;
        directTimeline.timeProperty().bind(editor.replayProperties().gameTimestamp());
        directTimeline.endProperty().bind(editor.replayProperties().replayDuration());
        speedSlider.valueProperty().bindBidirectional(editor.playbackSpeedProperty());
    }

    public ReplayEditor getEditor() {
        return editor;
    }
}
