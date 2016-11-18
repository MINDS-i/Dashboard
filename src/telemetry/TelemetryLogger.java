package com.telemetry;

import com.graph.DataSource;
import com.ui.SampleSource;
import com.Context;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

public class TelemetryLogger{
    private final static int DEFAULT_LOG_PERIOD = 250;
    static final DecimalFormat numberFormat = new DecimalFormat("#.########");
    private BufferedWriter logFile;
    private java.util.Timer logTimer;
    private int logPeriod;
    private long startTime;
    private TelemetryManager telemetry;
    private int previousTelemetryIndex;

    public TelemetryLogger(Context ctx, TelemetryManager tm){
        telemetry = tm;
        try {
            String logn = ctx.getInstanceLogName();
            FileWriter fileWriter = new FileWriter("log/"+logn+".telem", false);
            logFile = new BufferedWriter(fileWriter);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        setPeriod(DEFAULT_LOG_PERIOD);
        startTime = System.currentTimeMillis();
    }

    public void setPeriod(int period){
        if(logTimer != null) {
            logTimer.cancel();
            logTimer.purge();
        }
        logTimer = new java.util.Timer();
        TimerTask task = makeTimerTask();
        logTimer.scheduleAtFixedRate(task, period, period);
        logPeriod = period;
    }

    public int getPeriod(){
        return logPeriod;
    }

    private String formatLog(double t) {
        return numberFormat.format(t);
    }

    private TimerTask makeTimerTask() {
        return new TimerTask() {
            public void run() {
                try {
                    int tmIdx = telemetry.changeIndex();
                    if(tmIdx == previousTelemetryIndex) return;
                    previousTelemetryIndex = tmIdx;

                    long dt = System.currentTimeMillis() - startTime;
                    logFile.write(dt+", ");

                    for(int i=0; i<telemetry.maxIndex(); i++){
                        logFile.write(formatLog(telemetry.get(i)));
                        logFile.write(", ");
                    }

                    logFile.newLine();
                    logFile.flush();
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        };
    }
}
