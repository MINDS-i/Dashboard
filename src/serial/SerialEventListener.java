package com.serial;

import jssc.SerialPort;

public interface SerialEventListener {
    public void connectionEstablished(SerialPort newConnection);
    public void disconnectRequest();
}
