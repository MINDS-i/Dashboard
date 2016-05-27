package com.serial;

public interface Checksum {
    /**
     * return a byte[] containing the checksum of the plaintext array
     */
    public byte[] calc(byte[] plaintext);
    /**
     * return the length of a sum generated by this object
     */
    public int length();
}
