package com.igrium.replayeditorplus.util;

import static com.igrium.craftfx.util.ThreadUtils.onFXThread;

import org.jetbrains.annotations.Nullable;

import com.igrium.craftfx.util.ThreadUtils;
import com.replaymod.pathing.player.RealtimeTimelinePlayer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplaySender;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableObjectValue;

/**
 * Wraps various values from the Replay Mod as JavaFX properties.
 */
public final class ReplayProperties {
    
    private final ObjectProperty<ReplayHandler> handler = new SimpleObjectProperty<>();

    @Nullable
    private RealtimeTimelinePlayer timelinePlayer;

    public ReplayProperties() {
        this.handler.addListener((obs, oldVal, newVal) -> {
            timelinePlayer = newVal != null ? new RealtimeTimelinePlayer(newVal) : null;
            update();
        });
    }

    /**
     * The current replay handler.
     */
    public ObservableObjectValue<ReplayHandler> replayHandler() {
        return handler;
    }

    @Nullable
    public ReplayHandler getReplayHandler() {
        return handler.get();
    }

    public void setReplayHandler(@Nullable ReplayHandler replayHandler) {
        setVal(replayHandler, handler);
    }

    @Nullable
    public RealtimeTimelinePlayer getTimelinePlayer() {
        return timelinePlayer;
    }

    private DoubleProperty gameTimestamp = new SimpleDoubleProperty(0);

    /**
     * The current timestamp of the game in seconds, as returned by the replay sender.
     * @see ReplaySender#currentTimeStamp()
     */
    public ObservableDoubleValue gameTimestamp() {
        return gameTimestamp;
    }

    public double getGameTimestamp() {
        return gameTimestamp.get();
    }

    protected void setGameTimestamp(double timestamp) {
        if (timestamp == gameTimestamp.get()) return;
        ThreadUtils.onFXThread(() -> gameTimestamp.set(timestamp));
    }

    private BooleanProperty paused = new SimpleBooleanProperty(true);

    private DoubleProperty replayTimestamp = new SimpleDoubleProperty(0);
    
    /**
     * The current timestamp of the replay in seconds.
     */
    public ObservableDoubleValue replayTimestamp() {
        return replayTimestamp;
    }

    public double getReplayTimestamp() {
        return replayTimestamp.get();
    }

    /**
     * Whether the replay is paused.
     * @see ReplaySender#paused()
     */
    public ObservableBooleanValue paused() {
        return paused;
    }

    public boolean isPaused() {
        return paused.get();
    }

    private DoubleProperty replayDuration = new SimpleDoubleProperty();

    /**
     * The duration of the entire replay in seconds.
     * @see ReplayHandler#getReplayDuration()
     */
    public ObservableDoubleValue replayDuration() {
        return replayDuration;
    }

    public double getReplayDuration() {
        return replayDuration.get();
    }

    /**
     * Update all the values from the replay handler.
     */
    public void update() {
        ReplayHandler handler = this.handler.get();
        if (handler == null) return;

        setDouble(handler.getReplaySender().currentTimeStamp() / 1000d, gameTimestamp);
        setBool(handler.getReplaySender().paused(), paused);

        if (timelinePlayer.isActive()) {
            setDouble(timelinePlayer.getTimePassed() / 1000d, replayTimestamp);
        }

        setDouble(handler.getReplayDuration() / 1000d, replayDuration);
    }

    private <T> void setVal(T val, Property<T> target) {
        if (val.equals(target.getValue())) return;
        onFXThread(() -> target.setValue(val));
    }

    private void setBool(boolean val, BooleanProperty target)  {
        if (val == target.get()) return;
        onFXThread(() -> target.set(val));
    }
    
    private void setDouble(double val, DoubleProperty target) {
        if (val == target.get()) return;
        onFXThread(() -> target.set(val));
    }
}
