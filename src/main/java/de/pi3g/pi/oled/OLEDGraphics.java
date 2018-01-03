package de.pi3g.pi.oled;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.text.AttributedCharacterIterator;

/**
 * This is an implementation of java.awt.Graphics that allows you to draw to the framebuffer of an SSD1306
 * <p>
 * For any methods that have an ImageObserver argument, leaving it null will
 */
public class OLEDGraphics extends Graphics {

    protected Graphics g;
    protected BufferedImage img;
    protected OLEDDisplay display;
    protected byte[] frameBuffer;

    /**
     * Constructs a new <code>OLEDGraphics</code> object with the specified display
     *
     * @param display the Display to operate on
     */
    OLEDGraphics(OLEDDisplay display) {
        frameBuffer = new byte[display.getWidth() * display.getHeight() / 8];
        this.display = display;
        ColorModel cm = new IndexColorModel(1, 2, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255});
        DataBufferByte db = new DataBufferByte(frameBuffer, frameBuffer.length);
        WritableRaster r = VerticalMultiPixelPackedSampleModel.createRaster(db, display.getWidth(), display.getHeight(), 1, new Point());
        img = new BufferedImage(cm, r, false, null);
        g = img.getGraphics();
    }

    //todo: Added Framebuffer clear, export, and set

    /**
     * Pushes the current buffer of the Graphics object to the internal display buffer
     *
     * @throws IOException if unable to write bytes to the I2C bus
     */
    public void pushBuffer() throws IOException {
        display.writeDisplay(getFrameBuffer());
    }

    /**
     * Returns a reference to the graphics framebuffer.
     * <br>
     * Note: this is the framebuffer as it is currently in the graphics object,
     * not the internal framebuffer of the display. If changes have been made
     * since the last {@link #pushBuffer()} call, they will be different.
     *
     * @return The current framebuffer
     */
    protected byte[] getFrameBuffer() {
        return frameBuffer;
    }

    /**
     * {@inheritDoc}
     */
    public Graphics create() {
        return new OLEDGraphics(display);
    }

    /**
     * {@inheritDoc}
     */
    public void translate(int x, int y) {
        g.translate(x, y);
    }

    /**
     * {@inheritDoc}
     */
    public Color getColor() {
        return g.getColor();
    }

    /**
     * {@inheritDoc}
     */
    public void setColor(Color c) {
        g.setColor(c);
    }

    /**
     * {@inheritDoc}
     */
    public void setPaintMode() {
        g.setPaintMode();
    }

    /**
     * {@inheritDoc}
     */
    public void setXORMode(Color c1) {
        g.setXORMode(c1);
    }

    /**
     * {@inheritDoc}
     */
    public Font getFont() {
        return g.getFont();
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(Font font) {
        g.setFont(font);
    }

    /**
     * {@inheritDoc}
     */
    public FontMetrics getFontMetrics(Font f) {
        return g.getFontMetrics(f);
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle getClipBounds() {
        return g.getClipBounds();
    }

    /**
     * {@inheritDoc}
     */
    public void clipRect(int x, int y, int width, int height) {
        g.clipRect(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public void setClip(int x, int y, int width, int height) {
        g.setClip(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public Shape getClip() {
        return g.getClip();
    }

    /**
     * {@inheritDoc}
     */
    public void setClip(Shape clip) {
        g.setClip(clip);
    }

    /**
     * {@inheritDoc}
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        g.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * {@inheritDoc}
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    /**
     * {@inheritDoc}
     */
    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public void clearRect(int x, int y, int width, int height) {
        g.clearRect(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /**
     * {@inheritDoc}
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /**
     * {@inheritDoc}
     */
    public void drawOval(int x, int y, int width, int height) {
        g.drawOval(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public void fillOval(int x, int y, int width, int height) {
        g.fillOval(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    /**
     * {@inheritDoc}
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    /**
     * {@inheritDoc}
     */
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        g.drawPolyline(xPoints, yPoints, nPoints);
    }

    /**
     * {@inheritDoc}
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.drawPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * {@inheritDoc}
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.fillPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * {@inheritDoc}
     */
    public void drawString(String str, int x, int y) {
        g.drawString(str, x, y);
    }

    /**
     * {@inheritDoc}
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        g.drawString(iterator, x, y);
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, x, y, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, x, y, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, x, y, width, height, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, x, y, width, height, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, x, y, bgcolor, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, x, y, bgcolor, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, x, y, width, height, bgcolor, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, x, y, width, height, bgcolor, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        if (observer == null) {
            WaitingImageObserver waitObserver = new WaitingImageObserver();
            if (!g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, waitObserver))
                waitObserver.waitImageLoaded();
            return true;
        } else {
            return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        g.dispose();
    }
}
