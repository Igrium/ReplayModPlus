package com.igrium.replayeditorplus;

import com.igrium.craftfx.application.ApplicationManager;
import com.igrium.craftfx.application.ApplicationType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Identifier;

public class ReplayEditorPlus implements ClientModInitializer {

    public static final ApplicationType<ReplayEditor> EDITOR = ApplicationType
            .register(new Identifier("replayeditorplus", "editor"), new ApplicationType<>(ReplayEditor::new));
    
    private static ReplayEditorPlus instance;

    public static ReplayEditorPlus getInstance() {
        return instance;
    }

    private ReplayEditorModule module;

    @Override
    public void onInitializeClient() {
        instance = this;

        module = new ReplayEditorModule();
        module.register();
        module.initClient();
        
        WorldRenderEvents.START.register(context -> {
            ApplicationManager.getInstance().getAppInstance(EDITOR)
                    .ifPresent(editor -> editor.eachFrame());
        });
    }

    public ReplayEditorModule getModule() {
        return module;
    }
    
}
