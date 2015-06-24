package com.ui;

import com.ui.TelemetryListener;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;

public class DataLabel extends JLabel implements TelemetryListener{
  private String label;
  private String suffix;
  private double data;
  private void updateText(){
    String dataString = ((data >= 0)? " " : "") + String.format("%f", data);
    setText(label + dataString + suffix);
  }
  public DataLabel(String prefix, double dat, String units){
    label = prefix;
    data = dat;
    suffix = units;
    updateText();
  }

  public DataLabel(String prefix, String units){
    label = prefix;
    data = 0.;
    suffix = units;
    updateText();
  }

  public DataLabel(String prefix){
    label = prefix;
    data = 0.;
    suffix = new String();
    updateText();
  }
  public void update(double dat){
    data = dat;
    updateText();
  }

  public void setLabel(String prefix){
    label = prefix;
    updateText();
  }

  public void setUnits(String units){
    suffix = units;
    updateText();
  }

  public double getData(){
    return data;
  }
}
