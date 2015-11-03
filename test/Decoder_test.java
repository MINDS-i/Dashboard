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
    private String testChecksum = "C";
    private Checksum mockChecksum = mock(Checksum.class);

    public Decoder_test(){
        when(mockChecksum.length()).thenReturn(1);
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

    private String randomString(int length){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<length; i++){
            builder.append((char)(rand.nextInt(255)+1));
        }
        return builder.toString();
    }

    //test valid packet exceeds max length
    //test good/bad checksum
    //test random before/after
    //test bad packet before
    //test header in data
    //test larger header
    //test larger footer
    //test larger checksum
    //test multiple packet readers
    //test remove packet reader

    @Test
    public void testMatch() {
        for(int i=1; i<64; i++){
            String dataString = randomString(i);
            byte[] data = dataString.getBytes();

            PacketReader mRead = mock(PacketReader.class);
            when(mRead.claim(data[0])).thenReturn(data.length);
            InputStream stream = input(dataString);

            Decoder decoder = testDecoder(stream, mRead);
            decoder.update();

            verify(mRead).handle(data);
        }
    }
}
