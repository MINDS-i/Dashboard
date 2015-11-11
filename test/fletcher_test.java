import com.serial.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class fletcher_test {
    @Test
    public void testHelloWorldInt(){
        byte[] data = "Hello World!".getBytes();
        int sum = Serial.fletcher16(data);

        assertEquals(0x5941, sum);
    }

    @Test
    public void testRobotMessageInt(){
        byte[] data = "Robot Message".getBytes();
        int sum = Serial.fletcher16(data);

        assertEquals(0xf9ef, sum);
    }

    @Test
    public void emptyInt(){
        byte[] data = new byte[0];
        int sum = Serial.fletcher16(data);

        assertEquals(0xffff, sum);
    }

    @Test
    public void oneByteInt(){
        byte[] data = new byte[]{ 0x01 };
        int sum = Serial.fletcher16(data);

        assertEquals(0x0101, sum);
    }

    @Test
    public void zerosInt(){
        byte[] data = new byte[]{ 0x01, 0x00, 0x00, 0x00 };
        int sum = Serial.fletcher16(data);

        assertEquals(0x0401, sum);
    }

    @Test
    public void testHelloWorldBytes(){
        byte[] data = "Hello World!".getBytes();
        byte[] sum = Serial.fletcher16bytes(data);

        assertArrayEquals(new byte[]{(byte)0x59, (byte)0x41}, sum);
    }

    @Test
    public void testRobotMessageBytes(){
        byte[] data = "Robot Message".getBytes();
        byte[] sum = Serial.fletcher16bytes(data);

        assertArrayEquals(new byte[]{(byte)0xf9, (byte)0xef}, sum);
    }

    @Test
    public void emptyBytes(){
        byte[] data = new byte[0];
        byte[] sum = Serial.fletcher16bytes(data);

        assertArrayEquals(new byte[]{(byte)0xff, (byte)0xff}, sum);
    }

    @Test
    public void oneByteBytes(){
        byte[] data = new byte[]{ 0x01 };
        byte[] sum = Serial.fletcher16bytes(data);

        assertArrayEquals(new byte[]{(byte)0x01, (byte)0x01}, sum);
    }

    @Test
    public void zerosBytes(){
        byte[] data = new byte[]{ 0x01, 0x00, 0x00, 0x00 };
        byte[] sum = Serial.fletcher16bytes(data);

        assertArrayEquals(new byte[]{(byte)0x04, (byte)0x01}, sum);
    }
}
