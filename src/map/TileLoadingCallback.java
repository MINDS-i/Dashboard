package com.map;

public interface TileLoadingCallback {
    void started(String source, int total);
    void progress(String source, int number);
    void done(String source);
}
