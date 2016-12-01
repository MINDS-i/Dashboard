package com.ui;

import com.telemetry.TelemetryListener;
import com.ui.ninePatch.*;
import com.ui.ArtificialHorizon;
import com.Context;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.FontMetrics;

import javax.xml.stream.*;
import java.text.ParseException;

public class RadioWidget{
    public static JPanel create(Context ctx, int size){
        JPanel container = new TransparentPanel(ctx, size);

        return container;
    }
}
