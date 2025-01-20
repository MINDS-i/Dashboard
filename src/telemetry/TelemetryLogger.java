package com.telemetry;

import com.Context;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TimerTask;

public class TelemetryLogger {
    static final DecimalFormat numberFormat = new DecimalFormat("#.########");
    private final static int DEFAULT_LOG_PERIOD_MS = 250;
    private final long startTime;
    private final TelemetryManager telemetry;
    private BufferedWriter logFile;
    private java.util.Timer logTimer;
    private int logPeriod;
    private int previousTelemetryIndex;

    public TelemetryLogger(Context ctx, TelemetryManager tm) {
        telemetry = tm;
        try {
            String logn = ctx.getInstanceLogName();
            FileWriter fileWriter = new FileWriter("log/" + logn + ".telem", false);
            logFile = new BufferedWriter(fileWriter);
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
        setPeriod(DEFAULT_LOG_PERIOD_MS);
        startTime = System.currentTimeMillis();
    }

    public int getPeriod() {
        return logPeriod;
    }

    public void setPeriod(int period) {
        if (logTimer != null) {
            logTimer.cancel();
            logTimer.purge();
        }
        logTimer = new java.util.Timer();
        TimerTask task = makeTimerTask();
        logTimer.scheduleAtFixedRate(task, period, period);
        logPeriod = period;
    }

    private String formatLog(double t) {
        return numberFormat.format(t);
    }

    private TimerTask makeTimerTask() {
        return new TimerTask() {
            public void run() {
                try {
                    int tmIdx = telemetry.changeIndex();
                    if (tmIdx == previousTelemetryIndex) {
                        return;
                    }
                    previousTelemetryIndex = tmIdx;

                    long dt = System.currentTimeMillis() - startTime;
                    logFile.write(dt + ", ");

                    for (int i = 0; i < telemetry.maxIndex(); i++) {
                        logFile.write(formatLog(telemetry.get(i)));
                        logFile.write(", ");
                    }

                    logFile.newLine();
                    logFile.flush();
                }
                catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        };
    }
}
