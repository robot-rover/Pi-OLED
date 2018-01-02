package de.pi3g.pi.oled;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.io.OutputStream;

public class I2COutStream extends OutputStream {
    I2CDevice out;
    byte[] buffer;
    int index;
    public I2COutStream(int bufferSize, I2CDevice out) {
        buffer = new byte[bufferSize];
        index = 0;
        this.out = out;
    }

    public void write(int b) throws IOException {
        buffer[index] = reverseBitsByte((byte)(0xFF & b));
        //System.out.println(Integer.toHexString(0xFF & buffer[index]));
        index++;
        if(index >= buffer.length){
            out.write((byte) 0x40, buffer, 0, buffer.length);
            index = 0;
        }
    }

    public static byte reverseBitsByte(byte x) {
        byte b = 0;
        for (int i = 0; i < 8; i++){
            b<<=1;
            b|=( x &1);
            x>>=1;
        }
        return b;
    }


}
