package com.logging;

import java.util.logging.*;

public class EllipsisFormatter extends Formatter{
    private interface Content{ String get(LogRecord l); }
    private final Content content;
    private final int maxLineLength;
    /**
     * A formatter that ellipsizes each logRecord's raw message at maxLineLength
     */
    public EllipsisFormatter(int maxLineLength){
        super();
        content = (LogRecord l) -> l.getMessage();
        this.maxLineLength = maxLineLength;
    }
    /**
     * Make a formatter that ellipsizes the output of parent at maxLineLength
     */
    public EllipsisFormatter(Formatter parent, int maxLineLength){
        content = (LogRecord l) -> parent.format(l);
        this.maxLineLength = maxLineLength;
    }
    /** Fromat the given log record and return the formatted string */
    public String format(LogRecord lr){
        return ellipsize(content.get(lr));
    }
    /** Make a string one line of at most maxLineLength chars */
    private String ellipsize(String s){
        String line = (s.length() <= maxLineLength)? s
                        : s.substring(0,maxLineLength-3) + "...";
        return line.replace("\n"," ");
    }
}
