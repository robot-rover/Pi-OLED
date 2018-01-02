import de.pi3g.pi.oled.I2COutStream;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReverseBitsTest {
    public ReverseBitsTest(){}

    @Test
    public void testReverseBits(){

        assertEquals(100, 0xFF & I2COutStream.reverseBitsByte((byte) 38));
        assertEquals(0, 0xFF & I2COutStream.reverseBitsByte((byte) 0));
        assertEquals(255, 0xFF & I2COutStream.reverseBitsByte((byte) 255));
        assertEquals(17, 0xFF & I2COutStream.reverseBitsByte((byte) 136));
    }
}
