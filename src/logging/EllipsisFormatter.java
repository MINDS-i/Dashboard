package com.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class EllipsisFormatter extends Formatter {
    private final Content content;
    private final int maxLineLength;

    /**
     * A formatter that ellipsizes each logRecord's raw message at maxLineLength
     */
    public EllipsisFormatter(int maxLineLength) {
        super();
        content = (LogRecord l) -> l.getMessage();
        this.maxLineLength = maxLineLength;
    }

    /**
     * Make a formatter that ellipsizes the output of parent at maxLineLength
     */
    public EllipsisFormatter(Formatter parent, int maxLineLength) {
        content = (LogRecord l) -> parent.format(l);
        this.maxLineLength = maxLineLength;
    }

    /**
     * Make a string one line of at most length chars
     */
    public static String ellipsize(String s, int length) {
        String line = (s.length() <= length) ? s
                : s.substring(0, length - 3) + "...";
        return line.replace("\n", " ");
    }

    /**
     * Fromat the given log record and return the formatted string
     */
    public String format(LogRecord lr) {
        return ellipsize(content.get(lr), maxLineLength);
    }

    private interface Content {
        String get(LogRecord l);
    }
}
