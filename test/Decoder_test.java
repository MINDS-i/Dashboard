import com.serial.*;

import java.io.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Decoder_test {
    private Random rand = new Random();
    private String testHeader   = "H";
    private String testFooter   = "F";
    private String testChecksum = "CC";
    private Checksum mockChecksum = mock(Checksum.class);

    public Decoder_test(){
        when(mockChecksum.length()).thenReturn(testChecksum.getBytes().length);
        when(mockChecksum.calc(any(byte[].class))).thenReturn(testChecksum.getBytes());
    }

    private InputStream input(String header, String data, String checksum, String footer){
        String message = header+data+checksum+footer;
        return new ByteArrayInputStream(message.getBytes());
    }

    private InputStream input(String data){
        return input(testHeader, data, testChecksum, testFooter);
    }

    private Decoder testDecoder(InputStream is, PacketReader pr){
        Decoder decoder = new Decoder(is, testHeader.getBytes(),
                                          testFooter.getBytes(),
                                          mockChecksum);
        decoder.addPacketReader(pr);
        return decoder;
    }

    private Decoder testDecoder(String is, PacketReader pr){
        return testDecoder(input(is), pr);
    }

    private String randomString(int length){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<length; i++){
            builder.append((char)(rand.nextInt(255)+1));
        }
        return builder.toString();
    }

    @Test
    public void matchRandomData() {
        for(int i=1; i<64; i++){
            String dataString = randomString(i);
            byte[] data = dataString.getBytes();

            PacketReader mRead = mock(PacketReader.class);
            when(mRead.claim(data[0])).thenReturn(data.length);
            Decoder decoder = testDecoder(dataString, mRead);
            decoder.update();

            verify(mRead).handle(data);
        }
    }

    @Test
    public void longHeader(){
        String dataString = "longHeader";
        String longHeader = "abcdefghijklmnop";
        byte[] data = dataString.getBytes();
        InputStream stream = input(longHeader, dataString, testChecksum, testFooter);

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = new Decoder(stream, longHeader.getBytes(), testFooter.getBytes(), mockChecksum);
        decoder.addPacketReader(mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(data);
    }

    @Test
    public void longFooter(){
        String dataString = "longFooter";
        String longFooter = "abcdefghijklmnop";
        byte[] data = dataString.getBytes();
        InputStream stream = input(testHeader, dataString, testChecksum, longFooter);

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = new Decoder(stream, testHeader.getBytes(), longFooter.getBytes(), mockChecksum);
        decoder.addPacketReader(mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(data);
    }

    @Test
    public void longChecksum(){
        String dataString = "longSum";
        String longSum = "abcdefghijklmnop";
        byte[] data = dataString.getBytes();
        InputStream stream = input(testHeader, dataString, longSum, testFooter);

        PacketReader mRead = mock(PacketReader.class);
        Checksum checker = mock(Checksum.class);
        when(checker.length()).thenReturn(longSum.getBytes().length);
        when(checker.calc(any(byte[].class))).thenReturn(longSum.getBytes());
        Decoder decoder = new Decoder(stream, testHeader.getBytes(), testFooter.getBytes(), checker);
        decoder.addPacketReader(mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(data);
    }


    @Test
    public void badPacketBefore(){
        String dataString = "I'mTheGoodData";
        byte[] data = dataString.getBytes();
        InputStream stream = input("", testHeader+"I'mBadData"+testHeader+dataString+testChecksum+testFooter, "", "");

        PacketReader mRead = mock(PacketReader.class);
        Checksum checker = mock(Checksum.class);
        when(checker.length()).thenReturn(testChecksum.getBytes().length);
        when(checker.calc(data)).thenReturn(testChecksum.getBytes());
        Decoder decoder = new Decoder(stream, testHeader.getBytes(), testFooter.getBytes(), checker);
        decoder.addPacketReader(mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(data);
    }

    @Test
    public void multipleMatch(){
        String dataString = "I'mTheGoodData";
        byte[] data = dataString.getBytes();
        String packetString = testHeader+dataString+testChecksum+testFooter;

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(packetString+packetString+packetString, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead, times(3)).handle(data);
    }

    @Test
    public void multiplePacketTypes(){
        String firstString  = "FirstPacket";
        String secondString = "SecondPacket";
        String packetString = testHeader+firstString+testChecksum+testFooter+
                              testHeader+secondString+testChecksum+testFooter;
        InputStream stream = input("",packetString,"","");

        PacketReader aRead = mock(PacketReader.class);
        PacketReader bRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(stream, aRead);
        decoder.addPacketReader(bRead);
        when(aRead.claim(any(byte.class))).thenReturn(-1);
        when(bRead.claim(any(byte.class))).thenReturn(-1);
        when(aRead.claim((byte)'F')).thenReturn(firstString.getBytes().length);
        when(bRead.claim((byte)'S')).thenReturn(secondString.getBytes().length);
        decoder.update();

        verify(bRead).handle(secondString.getBytes());
        verify(aRead).handle(firstString.getBytes());
    }

    @Test
    public void decoyHeaderInData(){
        String dataString = testHeader+testHeader+testHeader;
        byte[] data = dataString.getBytes();

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(dataString, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(any(byte[].class));
    }

    @Test
    public void decoyFooterInData(){
        String dataString = testFooter+testFooter+testFooter;
        byte[] data = dataString.getBytes();

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(dataString, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead).handle(any(byte[].class));
    }

    @Test
    public void dontClaimMessage(){
        String dataString = "NotClaimed";
        byte[] data = dataString.getBytes();
        InputStream stream = input(testHeader, dataString, "", testFooter);

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(stream, mRead);
        when(mRead.claim(any(byte.class))).thenReturn(-1);
        decoder.update();

        verify(mRead, never()).handle(any(byte[].class));
    }

    @Test
    public void badChecksum(){
        String dataString = "NoChecksum";
        byte[] data = dataString.getBytes();
        InputStream stream = input(testHeader, dataString, "", testFooter);

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(stream, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.update();

        verify(mRead, never()).handle(any(byte[].class));
    }

    @Test
    public void packetTooLong(){
        String dataString = "I'mTooLong";
        byte[] data = dataString.getBytes();

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(dataString, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length-1);
        decoder.update();

        verify(mRead, never()).handle(any(byte[].class));
    }

    @Test
    public void removedPacketReader(){
        String dataString = "NobodyReadsMe";
        byte[] data = dataString.getBytes();

        PacketReader mRead = mock(PacketReader.class);
        Decoder decoder = testDecoder(dataString, mRead);
        when(mRead.claim(data[0])).thenReturn(data.length);
        decoder.removePacketReader(mRead);
        decoder.update();

        verify(mRead, never()).handle(any(byte[].class));
    }
}
