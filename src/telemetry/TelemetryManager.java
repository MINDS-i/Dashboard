package com.telemetry;

import com.graph.DataSource;
import com.ui.SampleSource;
import com.Context;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.regex.*;

public class TelemetryManager {
    private final List<Double> telemetry = new ArrayList<Double>();
    private final Map<Integer,List<TelemetryListener>> listenerMap =
        new HashMap<Integer,List<TelemetryListener>>();
    private final List<DataSource> streams = new ArrayList<DataSource>();
    private ResourceBundle labels = null;
    private int telemetryIndex = 0;

    public TelemetryManager(Context context) {
        String labelResPath = context.getResource("telemetryLabels");
        labels = context.loadResourceBundle(labelResPath);

        // initialize all the named values in the labels file
        Pattern labelRegex = Pattern.compile("t([0-9]+)");
        for(String label : labels.keySet()){
            Matcher m = labelRegex.matcher(label);
            if(m.matches()){
                updateTelemetry(Integer.parseInt(m.group(1)), 0.0);
            }
        }
    }
    public TelemetryManager(){
    }
    public String getTelemetryName(int index) {
        String resourceLabel = "t"+index;
        if(labels != null && labels.containsKey(resourceLabel)){
            return labels.getString(resourceLabel);
        }
        return "# "+index;
    }
    public List<DataSource> getDataSources() {
        return streams;
    }
    public void updateTelemetry(int id, double value) {
        update(id, value);
    }
    public void update(int id, double value){
        if(id >= telemetry.size()) {
            for(int i=telemetry.size(); i<=id; i++) {
                telemetry.add(0.0);
                SampleSource newSource = new SampleSource(getTelemetryName(i));
                this.registerListener(i, newSource);
                streams.add(newSource);
            }
        }
        telemetry.set(id, value);
        updateObservers(id, value);
        telemetryIndex += 1;
    }
    /**
     * An index that can be used to see if a update has been made to any
     *   telemetry values since the last time it was observed
     */
    public int changeIndex(){
        return telemetryIndex;
    }
    public double getTelemetry(int id) {
        return get(id);
    }
    public double get(int id){
        if(id >= telemetry.size()) return 0;
        return telemetry.get(id);
    }
    public int maxIndex() {
        return telemetry.size();
    }
    public void registerListener(int id, TelemetryListener tl) {
        List<TelemetryListener> listeners = listenerMap.get(id);
        if(listeners == null) {
            listeners = new LinkedList<TelemetryListener>();
            listenerMap.put(id, listeners);
        }
        listeners.add(tl);
    }
    public void removeListener(TelemetryListener tl) {
        for(List<TelemetryListener> ll : listenerMap.values()){
            ll.remove(tl);
        }
    }
    private void updateObservers(int id, double newValue) {
        List<TelemetryListener> listeners = listenerMap.get(id);
        if(listeners == null) return;
        for(TelemetryListener tl : listeners){
            tl.update(newValue);
        }
    }
}
