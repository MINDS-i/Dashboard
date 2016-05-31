package com.serial;

import jssc.SerialPort;

public interface SerialEventListener {
    /**
     * Fired when a successful connection is established
     * @param newConnection The new SerialPort object
     */
    public void connectionEstablished(SerialPort newConnection);
    /**
     * Fired when the user makes a request to disconnect the serial port
     * The SerialPort object will be closed when all disconnect Requests
     *     have finished
     * The handler should be prepared for future connections to be made after
     *     a disconnect
     */
    public void disconnectRequest();
}
