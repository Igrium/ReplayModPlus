package com.igrium.replayeditorplus;

import com.igrium.craftfx.application.ApplicationType;
import com.igrium.craftfx.application.CraftApplication;
import com.igrium.replayeditorplus.ui.ReplayEditorUI;
import com.igrium.replayeditorplus.util.ReplayProperties;
import com.replaymod.replay.ReplayHandler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.minecraft.client.MinecraftClient;

public class ReplayEditor extends CraftApplication {

    /**
     * Temp variable for if replay handler is set before JavaFX inits.
     */
    private ReplayHandler initReplayHandler;
    private ReplayProperties replayProperties;
    
    protected ReplayEditorUI editorUI;

    public ReplayEditor(ApplicationType<?> type, MinecraftClient client) {
        super(type, client);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(Stage primaryStage, Application parent) throws Exception {
        replayProperties = new ReplayProperties();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(ReplayEditorUI.FXML));
        Parent root = loader.load();
        editorUI = loader.getController();

        editorUI.setEditor(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        if (initReplayHandler != null) {
            replayProperties.setReplayHandler(initReplayHandler);
        }
    }

    public ReplayHandler getReplayHandler() {
        return replayProperties != null ? replayProperties.getReplayHandler() : initReplayHandler;
    }

    public void setReplayHandler(ReplayHandler replayHandler) {
        if (replayProperties != null) {
            replayProperties.setReplayHandler(replayHandler);
        } else {
            initReplayHandler = replayHandler;
        }
    }

    /**
     * A set of JavaFX observables that reflect various elements of replays and
     * replay handlers.
     */
    public ReplayProperties replayProperties() {
        return replayProperties;
    }

    @Override
    protected void onClosed() {
        editorUI.close();
    }
    
    void eachFrame() {
        if (replayProperties != null) replayProperties.update();
    }
}
