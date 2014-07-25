package com;

import com.serial.Serial;
import com.Context;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

public class Logger {

	private int 				period = 250;
	private FileWriter 			fileWriter;
	private BufferedWriter 		logFile;
	private java.util.Timer 	logTimer;
	private TimerTask 			task;
	private Context 			context;

	public Logger(Context cxt){
		context = cxt;
    	try{
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH-mm_MM-DD_yyyyGG");
			cal.getTime();
			String time = sdf.format(cal.getTime());

			fileWriter = new FileWriter("log/"+time+".log", false);
			logFile = new BufferedWriter(fileWriter);

			makeTimerTask();
			setLogPeriod(period);
	    } catch (IOException ex) {
	      System.err.println(ex);
	    }
	}

	public void setLogPeriod(int p){
		period = p;
		if(logTimer != null){
			logTimer.cancel();
			logTimer.purge();
		}
		logTimer = new java.util.Timer();
		makeTimerTask();
		logTimer.scheduleAtFixedRate(task, period, period);
	}

	public int getLogPeriod(){
		return period;
	}

	private void makeTimerTask(){
		task = new TimerTask(){
				public void run(){
					try{
						if(context.connected == false) return;
						for(int i=0; i<Serial.NUM_DATA_SLOTS; i++){
							if(context.isLogged[i])
								logFile.write(""+context.data[i]+" ");
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
