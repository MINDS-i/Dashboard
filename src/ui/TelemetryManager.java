package com.ui;

import com.ui.DataSource;
import com.ui.SampleSource;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;


public class TelemetryManager{
    private final static int DEFAULT_LOG_PERIOD = 250;

    private class Observer{
        private final int watched;
        private final TelemetryListener listener;
        Observer(int id, TelemetryListener tl){
            watched = id;
            listener = tl;
        }
        void update(double value){
            listener.update(value);
        }
        boolean watching(int id){
            return watched == id;
        }
        @Override
        public boolean equals(Object tl){
            if(tl.getClass() != this.getClass()) return false;
            return tl.equals(listener);
        }
    }

    private BufferedWriter       logFile;
    private java.util.Timer      logTimer;
    private int                  logPeriod;
    private List<Double>         telemetry;
    private Collection<Observer> observers;
    private List<DataSource>     streams;
    public TelemetryManager(){
        telemetry = new ArrayList<Double>();
        observers = new ArrayList<Observer>();
        streams   = new ArrayList<DataSource>();
        try{
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH-mm_MM-DD_yyyyGG");
            String time = sdf.format(cal.getTime());

            FileWriter fileWriter = new FileWriter("log/"+time+".log", false);
            logFile = new BufferedWriter(fileWriter);
            setLogPeriod(DEFAULT_LOG_PERIOD);
        } catch (IOException ex) {
          System.err.println(ex);
        }

        //build the first few telemetry locations up front
        updateTelemetry(8, 0.0d);
    }
    public List<DataSource> getDataSources(){
        return streams;
    }
    public void updateTelemetry(int id, double value){
        if(id >= telemetry.size()){
            for(int i=telemetry.size(); i<=id; i++){
                telemetry.add(0.0);
                SampleSource newSource = new SampleSource();
                this.registerListener(i, newSource);
                streams.add(newSource);
            }
        }
        telemetry.set(id, value);
        updateObservers(id);
    }
    public double getTelemetry(int id){
        if(id >= telemetry.size()) return 0;
        return telemetry.get(id);
    }
    public int telemetryCount(){
        return telemetry.size();
    }

    public void registerListener(int id, TelemetryListener tl){
        observers.add(new Observer(id,tl));
    }
    public void removeListener(TelemetryListener tl){
        observers.remove(new Observer(0, tl));
    }

    public void setLogPeriod(int period){
        if(logTimer != null){
            logTimer.cancel();
            logTimer.purge();
        }
        logTimer = new java.util.Timer();
        TimerTask task = makeTimerTask();
        logTimer.scheduleAtFixedRate(task, period, period);
        logPeriod = period;
    }
    public int getLogPeriod(){
        return logPeriod;
    }

    private void updateObservers(int id){
        for(Observer obs : observers){
            if(obs.watching(id)) obs.update(getTelemetry(id));
        }
    }
    static final DecimalFormat myFormatter = new DecimalFormat("#.########");
    private String formatLog(double t){
        return myFormatter.format(t);
    }
    private TimerTask makeTimerTask(){
        return new TimerTask(){
            public void run(){
                try{
                    for(double t : telemetry){
                        logFile.write(formatLog(t)+", ");
                    }
                    logFile.newLine();
                    logFile.flush();
                } catch (IOException ex){
                    System.err.println(ex);
                }
            }
        };
    }
}
