package com.igrium.replayeditorplus;

import com.google.common.collect.ImmutableMap;

import javafx.scene.input.KeyCode;
import net.minecraft.util.Identifier;

public final class ReplayKeybinds {
    private ReplayKeybinds() {};

    public static final Identifier PLAY_PAUSE = new Identifier("replayeditorplus", "play_pause");

    public static final ImmutableMap<KeyCode, Identifier> DEFAULTS = ImmutableMap.of(
        KeyCode.SPACE, PLAY_PAUSE
    );
}
