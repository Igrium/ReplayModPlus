package com.igrium.replayeditorplus;
import com.igrium.craftfx.application.ApplicationManager;
import com.replaymod.core.Module;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.events.ReplayOpenedCallback;

public class ReplayEditorModule extends EventRegistrations implements Module {

    public static ReplayEditorModule getInstance() {
        return ReplayEditorPlus.getInstance().getModule();
    }

    public ReplayEditorModule() {
        on(ReplayOpenedCallback.EVENT, this::onReplayOpened);
    }
    
    @Override
    public <T> EventRegistrations on(Event<T> event, T listener) {
        return super.on(event, listener);
    }

    protected void onReplayOpened(ReplayHandler replayHandler) {
        ApplicationManager.getInstance().launch(ReplayEditorPlus.EDITOR, editor -> editor.setReplayHandler(replayHandler));
    }
}
