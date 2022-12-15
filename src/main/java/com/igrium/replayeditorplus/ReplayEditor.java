package com.igrium.replayeditorplus;

import com.igrium.craftfx.application.ApplicationType;
import com.igrium.craftfx.application.CraftApplication;
import com.igrium.replayeditorplus.ui.ReplayEditorUI;
import com.replaymod.replay.ReplayHandler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.minecraft.client.MinecraftClient;

public class ReplayEditor extends CraftApplication {

    private ReplayHandler replayHandler;
    
    protected ReplayEditorUI editorUI;

    public ReplayEditor(ApplicationType<?> type, MinecraftClient client) {
        super(type, client);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(Stage primaryStage, Application parent) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ReplayEditorUI.FXML));
        Parent root = loader.load();
        editorUI = loader.getController();

        editorUI.setEditor(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public ReplayHandler getReplayHandler() {
        return replayHandler;
    }

    public void setReplayHandler(ReplayHandler replayHandler) {
        this.replayHandler = replayHandler;
    }

    @Override
    protected void onClosed() {
        editorUI.close();
    }
    
}
