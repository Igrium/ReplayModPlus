package com.igrium.replayeditorplus.util;

import com.replaymod.replaystudio.pathing.path.Timeline;

import net.minecraft.text.Text;

/**
 * Thrown when a timeline is unable to play.
 */
public class IllegalTimelineException extends Exception {
    private final Text messageText;
    private final Timeline source;
    
    /**
     * Create an IllegalTimelineException.
     * @param source The timeline that caused the exception.
     * @param message The exception message.
     */
    public IllegalTimelineException(Timeline source, Text message) {
        super(message.getString());
        this.messageText = message;
        this.source = source;
    }

    /**
     * Create an IllegalTimelineException.
     * @param source The timeline that caused the exception.
     * @param message The exception message.
     */
    public IllegalTimelineException(Timeline source, String message) {
        super(message);
        this.messageText = Text.literal(message);
        this.source = source;
    }

    /**
     * Get this exception's message as a Text object.
     * @return Exception message.
     */
    public Text getMessageText() {
        return messageText;
    }

    /**
     * Get the timeline that caused the exception.
     * @return The source timeline.
     */
    public Timeline getSource() {
        return source;
    }

}
