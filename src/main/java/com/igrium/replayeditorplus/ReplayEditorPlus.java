package com.igrium.replayeditorplus;

import org.apache.logging.log4j.LogManager;

import com.igrium.craftfx.application.ApplicationType;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class ReplayEditorPlus implements ClientModInitializer {

    public static final ApplicationType<ReplayEditor> EDITOR = ApplicationType
            .register(new Identifier("replayeditorplus", "editor"), new ApplicationType<>(ReplayEditor::new));

    @Override
    public void onInitializeClient() {
        LogManager.getLogger().info("Hello World!");
    }
    
}
