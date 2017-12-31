/*
 * Copyright (C) 2015 Florian Frankenberger.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.pi3g.pi.oled;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A raspberry pi driver for the 128x64 pixel OLED display (i2c bus).
 * The supported kind of display uses the SSD1306 driver chip and
 * is connected to the raspberry's i2c bus (bus 1).
 * <br>
 * Note that you need to enable i2c (for example, using {@code raspi-config}).
 * Also note that you need to load the following kernel modules:
 * <code>i2c-bcm2708</code> and <code>i2c_dev</code>
 * <br>
 * Also note that it is possible to speed up the refresh rate of the
 * display up to ~60fps by adding the following to the config.txt of
 * your raspberry: dtparam=i2c1_baudrate=1000000
 * <br><br>
 * Sample usage:
 * <pre>
 * OLEDDisplay display = new OLEDDisplay();
 * display.drawStringCentered("Hello World!", 25, true);
 * display.update();
 * Thread.sleep(10000); //sleep some time, because the display
 *                      //is automatically cleared the moment
 *                      //the application terminates
 * </pre>
 * This class is basically a rough port of Adafruit's BSD licensed
 * SSD1306 library (<a href="https://github.com/adafruit/Adafruit_SSD1306">https://github.com/adafruit/Adafruit_SSD1306</a>)
 *
 * @author Florian Frankenberger
 * @author robot_rover
 */
public class OLEDDisplay {

    private static final Logger LOGGER = Logger.getLogger(OLEDDisplay.class.getCanonicalName());

    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private static final int DEFAULT_DISPLAY_ADDRESS = 0x3C;

    private static final int DISPLAY_WIDTH = 128;
    private static final int DISPLAY_HEIGHT = 64;
    private static final int MAX_INDEX = (DISPLAY_HEIGHT / 8) * DISPLAY_WIDTH;

    private static final byte SSD1306_SETCONTRAST = (byte) 0x81;
    private static final byte SSD1306_DISPLAYALLON_RESUME = (byte) 0xA4;
    private static final byte SSD1306_DISPLAYALLON = (byte) 0xA5;
    private static final byte SSD1306_NORMALDISPLAY = (byte) 0xA6;
    private static final byte SSD1306_INVERTDISPLAY = (byte) 0xA7;
    private static final byte SSD1306_DISPLAYOFF = (byte) 0xAE;
    private static final byte SSD1306_DISPLAYON = (byte) 0xAF;

    private static final byte SSD1306_SETDISPLAYOFFSET = (byte) 0xD3;
    private static final byte SSD1306_SETCOMPINS = (byte) 0xDA;

    private static final byte SSD1306_SETVCOMDETECT = (byte) 0xDB;

    private static final byte SSD1306_SETDISPLAYCLOCKDIV = (byte) 0xD5;
    private static final byte SSD1306_SETPRECHARGE = (byte) 0xD9;

    private static final byte SSD1306_SETMULTIPLEX = (byte) 0xA8;

    private static final byte SSD1306_SETLOWCOLUMN = (byte) 0x00;
    private static final byte SSD1306_SETHIGHCOLUMN = (byte) 0x10;

    private static final byte SSD1306_SETSTARTLINE = (byte) 0x40;

    private static final byte SSD1306_MEMORYMODE = (byte) 0x20;
    private static final byte SSD1306_COLUMNADDR = (byte) 0x21;
    private static final byte SSD1306_PAGEADDR = (byte) 0x22;

    private static final byte SSD1306_COMSCANINC = (byte) 0xC0;
    private static final byte SSD1306_COMSCANDEC = (byte) 0xC8;

    private static final byte SSD1306_SEGREMAP = (byte) 0xA0;

    private static final byte SSD1306_CHARGEPUMP = (byte) 0x8D;

    private static final byte SSD1306_EXTERNALVCC = (byte) 0x1;
    private static final byte SSD1306_SWITCHCAPVCC = (byte) 0x2;

    private final I2CBus bus;
    private final I2CDevice device;

    private final byte[] imageBuffer = new byte[(DISPLAY_WIDTH * DISPLAY_HEIGHT) / 8];

    /**
     * Creates an OLED display object with default
     * i2c bus (1) and default display address (0x3C)
     *
     * @throws IOException if unable to initialize I2C bus or device
     */
    public OLEDDisplay() throws IOException {
        this(DEFAULT_I2C_BUS, DEFAULT_DISPLAY_ADDRESS);
    }

    /**
     * Creates an OLED display object with default
     * i2c bus (1) and the given display address
     *
     * @param displayAddress the i2c bus address of the display
     * @throws IOException if unable to initialize I2C bus or device
     */
    public OLEDDisplay(int displayAddress) throws IOException {
        this(DEFAULT_I2C_BUS, displayAddress);
    }

    /**
     * Creates an OLED display object with the given i2c bus and address
     *
     * @param busNumber the i2c bus number (use constants from I2CBus)
     * @param displayAddress the i2c bus address of the display
     * @throws IOException if unable to initialize I2C bus or device
     */
    public OLEDDisplay(int busNumber, int displayAddress) throws IOException {
        bus = I2CFactory.getInstance(busNumber);
        device = bus.getDevice(displayAddress);

        LOGGER.log(Level.FINE, "Opened i2c bus");

        clear();

        //add shutdown hook that clears the display
        //and closes the bus correctly when the software
        //if terminated.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        init();
    }

    /**
     * Clear the screen. All pixels are set off
     */
    public synchronized void clear() {
        Arrays.fill(imageBuffer, (byte) 0x00);
    }

    /**
     * Fills the screen with on or off pixels
     *
     * @param on Use on or off pixels
     */
    public synchronized void clear(boolean on) {
        Arrays.fill(imageBuffer, on ? (byte) 0xFF : (byte) 0x00);
    }

    public synchronized void setBuffer(byte[] src) {
        System.arraycopy(src, 0, imageBuffer, 0, imageBuffer.length);
    }

    /**
     * Get the width of the screen
     *
     * @return The screen width in pixels
     */
    public int getWidth() {
        return DISPLAY_WIDTH;
    }

    /**
     * Get the height of the screen
     *
     * @return The screen height in pixels
     */
    public int getHeight() {
        return DISPLAY_HEIGHT;
    }

    private void writeCommand(byte command) throws IOException {
        device.write(0x00, command);
    }

    private void init() throws IOException {
        writeCommand(SSD1306_DISPLAYOFF);                    // 0xAE
        writeCommand(SSD1306_SETDISPLAYCLOCKDIV);            // 0xD5
        writeCommand((byte) 0x80);                           // the suggested ratio 0x80
        writeCommand(SSD1306_SETMULTIPLEX);                  // 0xA8
        writeCommand((byte) 0x3F);
        writeCommand(SSD1306_SETDISPLAYOFFSET);              // 0xD3
        writeCommand((byte) 0x0);                            // no offset
        writeCommand((byte) (SSD1306_SETSTARTLINE | 0x0));   // line #0
        writeCommand(SSD1306_CHARGEPUMP);                    // 0x8D
        writeCommand((byte) 0x14);
        writeCommand(SSD1306_MEMORYMODE);                    // 0x20
        writeCommand((byte) 0x00);                           // 0x0 act like ks0108
        writeCommand((byte) (SSD1306_SEGREMAP | 0x1));
        writeCommand(SSD1306_COMSCANDEC);
        writeCommand(SSD1306_SETCOMPINS);                    // 0xDA
        writeCommand((byte) 0x12);
        writeCommand(SSD1306_SETCONTRAST);                   // 0x81
        writeCommand((byte) 0xCF);
        writeCommand(SSD1306_SETPRECHARGE);                  // 0xd9
        writeCommand((byte) 0xF1);
        writeCommand(SSD1306_SETVCOMDETECT);                 // 0xDB
        writeCommand((byte) 0x40);
        writeCommand(SSD1306_DISPLAYALLON_RESUME);           // 0xA4
        writeCommand(SSD1306_NORMALDISPLAY);

        writeCommand(SSD1306_DISPLAYON);//--turn on oled panel
    }

    /**
     * Changes the value of a pixel
     *
     * @param x The x coordinate of the pixel (Left is 0)
     * @param y The y coordinate of the pixel (Top is 0)
     * @param on Turn the pixel on or off4
     */
    public synchronized void setPixel(int x, int y, boolean on) {
        final int pos = x + (y / 8) * DISPLAY_WIDTH;
        if (pos >= 0 && pos < MAX_INDEX) {
            if (on) {
                this.imageBuffer[pos] |= (1 << (y & 0x07));
            } else {
                this.imageBuffer[pos] &= ~(1 << (y & 0x07));
            }
        }
    }

    /**
     * Draws a character on the screen
     *
     * @param c The character to draw
     * @param font The font to use
     * @param x The x coordinate of the left edge of the character
     * @param y The y coordinate of the top edge of the character
     * @param on Draw the character in on or off pixels
     */
    public synchronized void drawChar(char c, Font font, int x, int y, boolean on) {
        font.drawChar(this, c, x, y, on);
    }

    /**
     * Draws a string on the screen
     *
     * @param string The string to draw
     * @param font The font to use
     * @param x The x coordinate of the left edge of the string
     * @param y The y coordinate of the top edge of the string
     * @param on Draw the string in on or off pixels
     */
    public synchronized void drawString(String string, Font font, int x, int y, boolean on) {
        int posX = x;
        int posY = y;
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                posY += font.getOutterHeight();
                posX = x;
            } else {
                if (posX >= 0 && posX + font.getWidth() < this.getWidth()
                        && posY >= 0 && posY + font.getHeight() < this.getHeight()) {
                    drawChar(c, font, posX, posY, on);
                }
                posX += font.getOutterWidth();
            }
        }
    }

    /**
     * Draws a string centered in the middle of the screen
     *
     * @param string The string to draw
     * @param font The font to use
     * @param y The y coordinate of the top edge of the string
     * @param on Draw the string in on or off pixels
     */
    public synchronized void drawStringCentered(String string, Font font, int y, boolean on) {
        final int strSizeX = string.length() * font.getOutterWidth();
        final int x = (this.getWidth() - strSizeX) / 2;
        drawString(string, font, x, y, on);
    }

    /**
     * Fills a rectangular area of the screen with on or off pixels
     *
     * @param x The x coordinate of the left edge of the rectangle
     * @param y The y coordinate of the top edge of the rectangle
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param on Fill the area with on or off pixels
     */
    public synchronized void clearRect(int x, int y, int width, int height, boolean on) {
        for (int posX = x; posX < x + width; ++posX) {
            for (int posY = y; posY < y + height; ++posY) {
                setPixel(posX, posY, on);
            }
        }
    }

    /**
     * draws the given image over the current image buffer. The image
     * is automatically converted to a binary image (if it not already
     * is).
     * <br>
     * Note that the current buffer is not cleared before, so if you
     * want the image to completely overwrite the current display
     * content you need to call clear() before.
     *
     * @param image The image to draw
     * @param x The x coordinate of the left edge of the image
     * @param y The y coordinate of the top edge of the image
     */
    public synchronized void drawImage(BufferedImage image, int x, int y) {
        BufferedImage tmpImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        tmpImage.getGraphics().drawImage(image, x, y, null);

        int index = 0;
        int pixelval;
        final byte[] pixels = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        for (int posY = 0; posY < DISPLAY_HEIGHT; posY++) {
            for (int posX = 0; posX < DISPLAY_WIDTH / 8; posX++) {
                for (int bit = 0; bit < 8; bit++) {
                    pixelval = (byte) ((pixels[index/8] >>  (8 - bit)) & 0x01);
                    setPixel(posX * 8 + bit, posY, pixelval > 0);
                    index++;
                }
            }
        }
    }

    /**
     * sends the current buffer to the display
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    public synchronized void update() throws IOException {
        writeCommand(SSD1306_COLUMNADDR);
        writeCommand((byte) 0);   // Column start address (0 = reset)
        writeCommand((byte) (DISPLAY_WIDTH - 1)); // Column end address (127 = reset)

        writeCommand(SSD1306_PAGEADDR);
        writeCommand((byte) 0); // Page start address (0 = reset)
        writeCommand((byte) 7); // Page end address

        for (int i = 0; i < ((DISPLAY_WIDTH * DISPLAY_HEIGHT / 8) / 16); i++) {
            // send a bunch of data in one xmission
            device.write((byte) 0x40, imageBuffer, i * 16, 16);
        }
    }

    private synchronized void shutdown() {
        try {
            //before we shut down we clear the display
            clear();
            update();

            //now we close the bus
            bus.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Closing i2c bus");
        }
    }

}
