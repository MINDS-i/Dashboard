package com.remote;
/**
 * Data class representing the data and state of a remote robot's settings
 */
public class Setting {
    String name;
    String description;
    double min;
    double max;
    double def;
    double remoteVal;
    Setting() {
        this.name        = "";
        this.description = "";
        this.min         = 0;
        this.max         = 0;
        this.def         = 0;
    }
    Setting(String name, String description, double min, double max, double def) {
        this.name        = name;
        this.description = description;
        this.min         = min;
        this.max         = max;
        this.def         = def;
    }
    void setVal(double newVal) {
        remoteVal = newVal;
    }
    public Boolean outsideOfBounds(double v) {
        return (v < min) || (v > max);
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getMin() {
        return min;
    }
    public double getMax() {
        return max;
    }
    public double getDefault() {
        return def;
    }
    public double getVal() {
        return remoteVal;
    }
}
