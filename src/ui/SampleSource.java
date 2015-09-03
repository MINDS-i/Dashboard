package com.ui;

import com.graph.DataSource;
import java.util.List;
import java.util.ArrayList;
import java.awt.Paint;
import java.awt.Color;

public class SampleSource implements DataSource, TelemetryListener{
    static final int   SAMPLES = 1000;
    static final float SAT     = 0.90f;
    static final float BRIGHT  = 0.5f;
    private List<Double> data;
    private int oldestPosition;
    private final String name;
    public SampleSource(String name){
        this.name = name;
        data = new ArrayList<Double>(SAMPLES);
        for(int i=0; i<SAMPLES; i++) data.add(0.0d);
    }
    public void update(double d){
        data.set(oldestPosition, d);
        oldestPosition = (oldestPosition+1)%SAMPLES;
    }
    public double get(double x){
        if(x > 1.0 || x < 0.0) return 0.0;
        double xPoint = (x*((double)SAMPLES-1));
        int dataPos = ((int)Math.ceil(xPoint) +oldestPosition)%SAMPLES;
        int dataPrv = ((int)Math.floor(xPoint)+oldestPosition)%SAMPLES;
        double ratio = xPoint - Math.floor(xPoint);
        double rtn = data.get(dataPos) * ratio
                    +data.get(dataPrv) * (1.0d-ratio);
        return rtn;
    }
    public String getName(){
        return name;
    }
}
