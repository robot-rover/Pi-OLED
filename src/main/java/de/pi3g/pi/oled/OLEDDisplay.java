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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(OLEDDisplay.class);

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
    private OLEDGraphics graphics;

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
     * @param busNumber      the i2c bus number (use constants from I2CBus)
     * @param displayAddress the i2c bus address of the display
     * @throws IOException if unable to initialize I2C bus or device
     */
    public OLEDDisplay(int busNumber, int displayAddress) throws IOException {
        bus = I2CFactory.getInstance(busNumber);
        device = bus.getDevice(displayAddress);

        LOGGER.debug("I2C Bus Opened");

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
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    protected synchronized void clearInternalBuffer() throws IOException {
        setBufferPointer();
        byte[] clear = new byte[DISPLAY_WIDTH * DISPLAY_HEIGHT / 8];
        Arrays.fill(clear, (byte) 0x00);
        device.write(0x40, clear, 0, clear.length);
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

    /**
     * Writes a single byte to the command address of the display I2C device
     *
     * @param command The byte to write
     * @throws IOException if unable to write bytes to the I2C bus
     */
    private void writeCommand(byte command) throws IOException {
        device.write(0x00, command);
    }

    public OLEDGraphics getGraphics() {
        return graphics == null ? (graphics = new OLEDGraphics(this)) : graphics;
    }

    /**
     * Writes over the I2C bus to the framebuffer of the display.
     * It is advisable to call {@link #setBufferPointer()} beforehand
     * in order to ensure you start at (0, 0)
     *
     * @param buffer the framebuffer to write to the display
     * @throws IOException if unable to write bytes to the I2C bus
     */
    protected void writeDisplay(byte[] buffer) throws IOException {
        setBufferPointer();
        device.write(0x40, buffer, 0, buffer.length);
    }

    /**
     * Sets up the display with the correct memory addressing, display clock, etc.
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    private void init() throws IOException {
        writeCommand(SSD1306_DISPLAYOFF);                    // 0xAE
        writeCommand(SSD1306_SETDISPLAYCLOCKDIV);            // 0xD5
        writeCommand((byte) 0x80);                           // the suggested ratio 0x80
        writeCommand(SSD1306_SETMULTIPLEX);                  // 0xA8
        writeCommand((byte) 0x3F);
        writeCommand(SSD1306_SETDISPLAYOFFSET);              // 0xD3
        writeCommand((byte) 0x0);                            // no offset
        writeCommand((byte) (SSD1306_SETSTARTLINE | 0x0));   // line #0 - 0x01 for line #1
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
        clearInternalBuffer();

        writeCommand(SSD1306_DISPLAYON);//--turn on oled panel
    }

    /**
     * Powers on or off the display hardware
     *
     * @param on turn the display on or off
     * @throws IOException if unable to write bytes to the I2C bus
     */
    public synchronized void powerDisplay(boolean on) throws IOException {
        writeCommand(on ? SSD1306_DISPLAYON : SSD1306_DISPLAYOFF);
    }

    /**
     * Turns on or off the display hardware's invert feature
     *
     * @param invert turn inversion on or off
     * @throws IOException if unable to write bytes to the I2C bus
     */
    public synchronized void invertDisplay(boolean invert) throws IOException {
        writeCommand(invert ? SSD1306_INVERTDISPLAY : SSD1306_NORMALDISPLAY);
    }

    /**
     * Sets the display memory pointer back to (0, 0)
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    protected void setBufferPointer() throws IOException {
        writeCommand(SSD1306_COLUMNADDR);
        writeCommand((byte) 0);   // Column start address (0 = reset)
        writeCommand((byte) (DISPLAY_WIDTH - 1)); // Column end address (127 = reset)

        writeCommand(SSD1306_PAGEADDR);
        writeCommand((byte) 0); // Page start address (0 = reset)
        writeCommand((byte) 7); // Page end address
    }

    /**
     * Cleans up the display and closes the bus
     */
    private synchronized void shutdown() {
        try {
            //before we shut down we clear the display
            clearInternalBuffer();

            //now we close the bus
            bus.close();
            LOGGER.debug("I2C Bus closed");
        } catch (IOException ex) {
            LOGGER.warn("Unable to close I2C Bus", ex);
        }
    }

    /**
     * Shows a test image on the screen.
     * Intended to test if the display hardware
     * is configured correctly.
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    public synchronized void test() throws IOException {
        writeCommand(SSD1306_COLUMNADDR);
        writeCommand((byte) 0);   // Column start address (0 = reset)
        writeCommand((byte) 127); // Column end address (127 = reset)

        writeCommand(SSD1306_PAGEADDR);
        writeCommand((byte) 0); // Page start address (0 = reset)
        writeCommand((byte) 7); // Page end address
        for (int i = 0; i < 128 * 8; i++)
            device.write(0x40, (byte) i);
    }

}
