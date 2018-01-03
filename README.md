Pi OLED Display
===============

A Java library to drive the popular monochrome 128x64 pixel OLED display (SSD1306).
The display can be bought from Adafruit or from a lot of ebay vendors.

This is basically a rough port of Adafruit's SSD1306 library for Arduino which
can be found here: https://github.com/adafruit/Adafruit_SSD1306

This extension of entrusc's original library uses java's java.awt.Graphics to perform operations on the display, 
which is a more fully featured api than that included in the original.

How to Use?
============
You can then use the library in your maven projects like this:

    <dependency>
        <groupId>de.pi3g.pi</groupId>
        <artifactId>pi-oled</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

The hardware should be connected to the i2c bus. Where the i2c bus pins
are located can be looked up e.g. here: 
http://elinux.org/RPi_Low-level_peripherals#General_Purpose_Input.2FOutput_.28GPIO.29 

Then you can use the library like this:

    OLEDDisplay display = new OLEDDisplay();
    OLEDGraphics g = display.getGraphics();
    g.drawLine(0, 0, 64, 64);
    g.drawString("Hello World!", 16, 16);
    g.pushBuffer();

Note that you always need to call OLEDGraphics#pushBuffer() after you changed the content of the display
to actually get the content displayed on the hardware.

Also note that the default constructor assumes you have connected the display to
i2c port 1 and the display's i2c address is 0x3C. If this is not the case you
can use one of the constructors with more parameters.

How to Build?
=============

The entire project is built with maven. Just clone the master branch, open the directory in an IDE and hit run. Or if
you prefer the command line: 

    mvn install

should build everything correctly. 