package com.ui;

import com.ui.DataSource;
import com.ui.TelemetryListener;

import java.awt.Paint;
import java.awt.Color;

public class TelemetryDataSource implements DataSource, TelemetryListener{
    public void update(double data){

    }
    public double get(double x){
        return 0;
    }
    public Paint getPaint(){
        return (Paint) Color.BLUE;
    }
}
