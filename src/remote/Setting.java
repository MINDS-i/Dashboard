package com.remote;
/**
 * Data class representing the data and state of a remote robot's settings
 */
public class Setting {
    String name;
    String description;
    float min;
    float max;
    float def;
    float remoteVal;
    Setting() {
        this.name        = "";
        this.description = "";
        this.min         = 0;
        this.max         = 0;
        this.def         = 0;
    }
    Setting(String name, String description, float min, float max, float def) {
        this.name        = name;
        this.description = description;
        this.min         = min;
        this.max         = max;
        this.def         = def;
    }
    void setVal(float newVal) {
        remoteVal = newVal;
    }
    public Boolean outsideOfBounds(float v) {
        return (v < min) || (v > max);
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public float getMin() {
        return min;
    }
    public float getMax() {
        return max;
    }
    public float getDefault() {
        return def;
    }
    public float getVal() {
        return remoteVal;
    }
}
