package com.igrium.replayeditorplus;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.igrium.craftfx.application.ApplicationType;
import com.igrium.craftfx.application.CraftApplication;
import com.igrium.craftfx.util.ThreadUtils;
import com.igrium.replayeditorplus.ui.ReplayEditorUI;
import com.igrium.replayeditorplus.util.IllegalTimelineException;
import com.igrium.replayeditorplus.util.ReplayProperties;
import com.replaymod.pathing.player.RealtimeTimelinePlayer;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplaySender;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.SPTimeline;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ReplayEditor extends CraftApplication {

    /**
     * Temp variable for if replay handler is set before JavaFX inits.
     */
    private ReplayHandler initReplayHandler;
    private ReplayProperties replayProperties;
    
    protected ReplayEditorUI editorUI;
    
    private final Map<KeyCode, Identifier> keybinds = new HashMap<>();
    public final ReplayModSimplePathing mod = ReplayModSimplePathing.instance;

    public ReplayEditor(ApplicationType<?> type, MinecraftClient client) {
        super(type, client);
        keybinds.putAll(ReplayKeybinds.DEFAULTS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(Stage primaryStage, Application parent) throws IOException {
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

    public void toggleTimelinePause() {

    }

    /**
     * Stop playing the current timeline. If the timeline is not playing, this does
     * nothing.
     */
    public void stopTimeline() {
        ThreadUtils.onRenderThread(() -> replayProperties.getTimelinePlayer().getFuture().cancel(false));
    }

    /**
     * Attempt to start playing the current timeline. This is a high-level function
     * that displays alerts instead of throwing.
     * 
     * @param startTime The time to start playing at, in milliseconds.
     */
    public void startTimeline(int startTime) {
        ThreadUtils.onRenderThread(() -> startTimelineImpl(startTime));
    }
    

    private void startTimelineImpl(int startTime) {
        RealtimeTimelinePlayer player = replayProperties.getTimelinePlayer();
        if (player.isActive()) {
            player.getFuture().cancel(false);
        }

        Timeline timeline;
        try {
            timeline = preparePathsForPlayback(false);
        } catch (IllegalTimelineException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Unable to start playback.");
                alert.setContentText(e.getMessage());
                alert.initOwner(getStage());
                alert.initModality(Modality.APPLICATION_MODAL);
    
                alert.show();
            });
            return;
        }

        ListenableFuture<Void> future = player.start(timeline, startTime);
        
        Futures.addCallback(future, new FutureCallback<>() {

            @Override
            public void onSuccess(Void arg0) {
                if (future.isCancelled()) {
                    mod.getCore().printInfoToChat("replaymod.chat.pathinterrupted");
                } else {
                    mod.getCore().printInfoToChat("replaymod.chat.pathfinished");
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (!(e instanceof CancellationException)) {
                    LogManager.getLogger().error("Error playing back replay", e);
                }
            }
            
        }, ThreadUtils::onRenderThread);
    }

    /**
     * Prepare the paths in this replay file for playback.
     * 
     * @param ignoreTimeKeyframes Don't check whether there's enough time
     *                            keyframnes.
     * @return A timeline with the playable paths.
     * @throws IllegalTimelineException If an error in the active timeline makes it
     *                                  unable to play.
     */
    public Timeline preparePathsForPlayback(boolean ignoreTimeKeyframes) throws IllegalTimelineException {
        SPTimeline spTimeline = mod.getCurrentTimeline();
        validatePathsForPlayback(spTimeline, ignoreTimeKeyframes);
        
        try {
            TimelineSerialization serialization = new TimelineSerialization(spTimeline, null);
            String serialized = serialization.serialize(Collections.singletonMap("", spTimeline.getTimeline()));
            Timeline timeline = serialization.deserialize(serialized).get("");
            timeline.getPaths().forEach(Path::updateAll);
            return timeline;
        } catch (Exception e) {
            throw new RuntimeException("Error cloning timeline.", e);
        }

    }

    private void validatePathsForPlayback(SPTimeline timeline, boolean ignoreTimeKeyframes)
            throws IllegalTimelineException {
        timeline.getTimeline().getPaths().forEach(Path::updateAll);

        if (timeline.getPositionPath().getSegments().isEmpty()) {
            throw new IllegalTimelineException(timeline.getTimeline(), Text.translatable("replaymod.chat.morekeyframes"));
        }

        if (ignoreTimeKeyframes) return;

        int lastTime = 0;
        for (Keyframe keyframe : timeline.getTimePath().getKeyframes()) {
            int time = keyframe.getValue(TimestampProperty.PROPERTY).orElseThrow(IllegalStateException::new);
            if (time < lastTime) {
                throw new IllegalTimelineException(timeline.getTimeline(),
                        Text.empty().append(Text.translatable("replaymod.chat.morekeyframes")
                                .append(" ").append("replaymod.error.negativetime2")
                                .append(" ").append("replaymod.error.negativetime3")));
            }
        }

        if (timeline.getTimePath().getSegments().isEmpty()) {
            throw new IllegalTimelineException(timeline.getTimeline(), Text.translatable("replaymod.chat.morekeyframes"));
        }
    }
    
    void eachFrame() {
        if (replayProperties != null) replayProperties.update();
    }
}
