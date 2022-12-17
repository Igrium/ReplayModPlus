package com.igrium.replayeditorplus.ui;

import com.igrium.craftfx.viewport.EngineViewport;
import com.igrium.craftfx.viewport.PrimaryViewport;
import com.igrium.craftfx.viewport.StandardInputController;
import com.igrium.replayeditorplus.ReplayEditor;
import com.igrium.replayeditorplus.ui.controls.TimelineUI;

import javafx.fxml.FXML;

public class ReplayEditorUI {
    public static final String FXML = "/assets/replayeditorplus/ui/editor_ui.fxml";

    @FXML
    private PrimaryViewport primaryViewport;

    @FXML
    private TimelineUI timeline;

    private StandardInputController<?> inputController;
    protected ReplayEditor editor;

    public final EngineViewport getPrimaryViewport() {
        return primaryViewport;
    }

    public final StandardInputController<?> getInputController() {
        return inputController;
    }

    public ReplayEditor getEditor() {
        return editor;
    }

    /**
     * Internal use only
     */
    @Deprecated
    public void setEditor(ReplayEditor editor) {
        this.editor = editor;
    }
    

    @FXML
    protected void initialize() {
        inputController = new StandardInputController<EngineViewport>(primaryViewport);
    }

    public void close() {
        primaryViewport.close();
        inputController.close();
    }
}
