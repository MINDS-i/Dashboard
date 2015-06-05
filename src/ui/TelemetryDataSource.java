package com.ui;

import com.ui.DataSource;
import com.ui.TelemetryListener;
import java.util.LinkedList;
import java.awt.Paint;
import java.awt.Color;

public class TelemetryDataSource implements DataSource, TelemetryListener{
    static final int SAMPLES = 400;
    static final float SAT = 0.90f;
    static final float BRIGHT = 0.5f;
    private LinkedList<Double> data;
    private Graph graph;
    private Paint paint;
    public TelemetryDataSource(int id, TelemetryManager tm, Graph g){
        data = new LinkedList<Double>();
        tm.registerListener(id, this);
        graph = g;
        float hue = (float)Math.random();
        paint = (Paint) Color.getHSBColor(hue, SAT, BRIGHT);
    }
    public void update(double d){
        data.add(d);
        if(data.size() > SAMPLES) data.remove();
        graph.repaint();
    }
    public double get(double x){
        int xPoint = (int) (x*((double)SAMPLES));

        if(xPoint >= data.size()) return 0.0;
        return data.get( xPoint ) / 180.0;
    }
    public Paint getPaint(){
        return paint;
    }
}
