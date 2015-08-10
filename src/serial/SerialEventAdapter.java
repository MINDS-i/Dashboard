package com.serial;

import jssc.SerialPort;
import com.serial.SerialEventListener;

public class SerialEventAdapter implements SerialEventListener {
    public void connectionEstablished(SerialPort newConnection){
    }
    public void disconnectRequest(){
    }
}
