package com.igrium.replayeditorplus;

import org.apache.logging.log4j.LogManager;

import net.fabricmc.api.ClientModInitializer;

public class ReplayEditorPlus implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LogManager.getLogger().info("Hello World!");
    }
    
}
