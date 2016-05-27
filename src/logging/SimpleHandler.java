package com.logging;

import java.util.logging.*;

/**
 * SimpleHandler is a minimal implementation of Handler for convenience.
 * It respects its assigned level, filter, and formatter, then gives the
 * final strings to a MessageUser instance
 */
public class SimpleHandler extends Handler {
    public interface MessageUser {
        /** Consume a filtered, formatted log message */
        public void use(LogRecord l, String message);
    }
    private final MessageUser messageUser;
    public SimpleHandler(MessageUser messageUser) {
        super();
        this.messageUser = messageUser;
    }
    public void close() {}
    public void flush() {}
    public void publish(LogRecord record) {
        // Check that its a level we care about
        Level level = getLevel();
        if(level != null && record.getLevel().intValue() < level.intValue())
            return;
        // See if the filter OK's the message
        Filter filter = getFilter();
        if(filter != null && !filter.isLoggable(record))
            return;
        // Format and consume the message
        Formatter format = getFormatter();
        String msg = (format==null)? record.getMessage()
                     : format.format(record);
        messageUser.use(record, msg);
    }
}
