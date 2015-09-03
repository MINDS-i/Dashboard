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
  private int maxWidth;
  private void updateText(){
    StringBuilder out = new StringBuilder();
    out.append(label);
    out.append((data >= 0)? " " : "-");
    out.append(String.format("%f", Math.abs(data)));
    out.append(suffix);

    int finalWidth = Math.min(out.length(), maxWidth);
    setText(out.substring(0,finalWidth));
  }
  public DataLabel(String prefix, double dat, String units){
    label    = prefix;
    data     = dat;
    suffix   = units;
    maxWidth = Integer.MAX_VALUE;
    updateText();
  }
  public DataLabel(String prefix, String units){
    this(prefix, 0.0, units);
  }
  public DataLabel(String prefix){
    this(prefix, 0.0, "");
  }
  public void setMaxLength(int ml){
    maxWidth = ml;
  }
  public void update(double data){
    this.data = data;
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
