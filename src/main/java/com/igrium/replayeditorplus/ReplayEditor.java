package com.igrium.replayeditorplus;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.igrium.craftfx.application.ApplicationType;
import com.igrium.craftfx.application.CraftApplication;
import com.igrium.craftfx.util.ThreadUtils;
import com.igrium.replayeditorplus.ui.ReplayEditorUI;
import com.igrium.replayeditorplus.util.ReplayProperties;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplaySender;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class ReplayEditor extends CraftApplication {

    /**
     * Temp variable for if replay handler is set before JavaFX inits.
     */
    private ReplayHandler initReplayHandler;
    private ReplayProperties replayProperties;
    
    protected ReplayEditorUI editorUI;
    
    private final Map<KeyCode, Identifier> keybinds = new HashMap<>();

    public ReplayEditor(ApplicationType<?> type, MinecraftClient client) {
        super(type, client);
        keybinds.putAll(ReplayKeybinds.DEFAULTS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(Stage primaryStage, Application parent) throws Exception {
        replayProperties = new ReplayProperties();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(ReplayEditorUI.FXML));
        Parent root = loader.load();
        editorUI = loader.getController();

        editorUI.initEditor(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        if (initReplayHandler != null) {
            replayProperties.setReplayHandler(initReplayHandler);
        }
        
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (ReplayKeybinds.PLAY_PAUSE.equals(keybinds.get(e.getCode()))) {
                editorUI.getTimelineWindow().onPlayPause();
                e.consume();
            }
        });
        
        playbackSpeedProperty.addListener((obs, oldVal, newVal) -> {
            if (replayProperties.isPaused()) return;
            setReplayPlayback(newVal.doubleValue());
        });
    }

    private final DoubleProperty playbackSpeedProperty = new SimpleDoubleProperty(1);

    public double getPlaybackSpeed() {
        return playbackSpeedProperty.get();
    }

    public void setPlaybackSpeed(double playbackSpeed) {
        playbackSpeedProperty.set(playbackSpeed);
    }

    public DoubleProperty playbackSpeedProperty() {
        return playbackSpeedProperty;
    }

    @Nullable
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

    public void setPaused(boolean paused) {
        if (paused) {
            setReplayPlayback(0);
        } else {
            setReplayPlayback(getPlaybackSpeed());
        }
    }

    public void togglePause() {
        setPaused(!replayProperties.isPaused());
    }

    /**
     * Invoke <code>setReplaySpeed()</code> on the current replay sender.
     * @param speed The speed factor.
     * @see ReplaySender#setReplaySpeed(double)
     */
    private void setReplayPlayback(double speed) {
        ReplayHandler handler = getReplayHandler();
        if (handler == null) return;

        ThreadUtils.onRenderThread(() -> handler.getReplaySender().setReplaySpeed(speed));
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

    public Map<KeyCode, Identifier> getKeybinds() {
        return keybinds;
    }
    
    void eachFrame() {
        if (replayProperties != null) replayProperties.update();
    }
}
