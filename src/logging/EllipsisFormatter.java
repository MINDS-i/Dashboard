package com.logging;

import java.util.logging.*;

public class EllipsisFormatter extends Formatter{
    /**
     * A formatter that ellipsizes each logRecord's raw message at maxLineLength
     */
    public EllipsisFormatter(int maxLineLength){

    }
    /**
     * Make a formatter that ellipsizes the output of parent at maxLineLength
     */
    public EllipsisFormatter(Formatter parent, int maxLineLength){

    }
    /** Fromat the given log record and return the formatted string */
    public String format(LogRecord lr){
        return "";
    }
    private String ellipsize(String s){
        return "";
    }
}
