package de.pi3g.pi.oled;

import org.junit.Test;

import java.awt.*;
import java.awt.image.*;

import static org.junit.Assert.assertArrayEquals;

public class SamplerTest {
    private static String previewFramebuffer(byte[] pixels) {
        System.out.println("ArrayLength: " + pixels.length);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pixels.length; i++) {
            builder.append(String.format("%-3s", Integer.toHexString(0xFF & pixels[i])));
            if ((i + 1) % 16 == 0)
                builder.append("\n");
        }
        return builder.toString();
    }

    private static String previewImageBytes(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.out.println("Width: " + image.getWidth() + ", Height: " + image.getHeight());
        return previewFramebuffer(pixels);

    }

    @Test
    public void SamplerGetterTest() {
        byte[] bufContents = new byte[]{
                0x00, 0x01, 0x02, 0x07,
                0x00, 0x0F, 0x01, 0x0E

        };
        VerticalMultiPixelPackedSampleModel sm = new VerticalMultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, 4, 16, 1);
        DataBufferByte buf = new DataBufferByte(bufContents, bufContents.length);
        assertArrayEquals(new int[]{0}, sm.getPixel(0, 0, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(1, 0, new int[1], buf));
        assertArrayEquals(new int[]{0}, sm.getPixel(2, 0, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(2, 1, new int[1], buf));
        assertArrayEquals(new int[]{0}, sm.getPixel(2, 2, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(3, 0, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(3, 1, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(3, 2, new int[1], buf));
        assertArrayEquals(new int[]{1}, sm.getPixel(1, 8, new int[1], buf));

    }

    @Test
    public void GraphicsLineTest() {
        byte[] bufContents = new byte[32];
        byte[] finalBuf = new byte[]{
                0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80
        };
        DataBufferByte buf = new DataBufferByte(bufContents, bufContents.length);
        WritableRaster r = VerticalMultiPixelPackedSampleModel.createRaster(buf, 16, 16, 1, new Point());
        ColorModel cm = new IndexColorModel(1, 2, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255});
        BufferedImage image = new BufferedImage(cm, r, false, null);
        Graphics g = image.getGraphics();
        g.drawLine(0, 0, 15, 15);
        System.out.println(previewImageBytes(image));
        assertArrayEquals(finalBuf, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());

    }
}
