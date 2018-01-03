
package de.pi3g.pi.oled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.*;

/**
 * The <code>VerticalMultiPixelPackedSampleModel</code> class represents
 * one-banded images and can pack multiple one-sample
 * pixels into one data element, with each element representing a column of pixels rather than a row.
 * Pixels are not allowed to span data elements.
 * The data type can be DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT,
 * or DataBuffer.TYPE_INT.  Each pixel must be a power of 2 number of bits
 * and a power of 2 number of pixels must fit exactly in one data element.
 * Pixel bit stride is equal to the number of bits per pixel.
 * Data bit offset is the offset in bits from
 * the beginning of the {@link DataBuffer} to the first pixel and must be
 * a multiple of pixel bit stride. This feature is currently untested.
 * <p>
 * This class is a rough rewrite of Java's java.awt.image.MultiPixelPackedSampleModel,
 * but with each element representing a column of pixels instead of a row,
 * in order to match the memory layout of the SSD1306 chip and avoid
 * costly array reshuffling every time we want to write to the framebuffer.
 */
public class VerticalMultiPixelPackedSampleModel extends SampleModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticalMultiPixelPackedSampleModel.class);

    /**
     * The number of bits from one pixel to the next.
     */
    int pixelBitStride;

    /**
     * Bitmask that extracts the topmost pixel of a data element.
     */
    int bitMask;

    /**
     * The number of pixels that fit in a data element.
     */
    int pixelsPerDataElement;

    /**
     * The size of a data element in bits.
     */
    int dataElementSize;

    /**
     * The bit offset into the data array where the first pixel begins.
     * Nonzero values are currently <b>Untested</b>.
     */
    int dataBitOffset;

    /**
     * Constructs a <code>VerticalMultiPixelPackedSampleModel</code> with the
     * specified data type, width, height and number of bits per pixel.
     *
     * @param dataType     the data type for storing samples
     * @param w            the width, in pixels, of the region of
     *                     image data described
     * @param h            the height, in pixels, of the region of
     *                     image data described
     * @param numberOfBits the number of bits per pixel
     * @throws IllegalArgumentException if <code>dataType</code> is not
     *                                  either <code>DataBuffer.TYPE_BYTE</code>,
     *                                  <code>DataBuffer.TYPE_USHORT</code>, or
     *                                  <code>DataBuffer.TYPE_INT</code>
     * @throws IllegalArgumentException if <code>w</code> or
     *                                  <code>h</code> is not greater than 0
     */
    public VerticalMultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits) {
        this(dataType, w, h, numberOfBits, 0);
    }

    /**
     * Constructs a <code>MultiPixelPackedSampleModel</code> with
     * specified data type, width, height, number of bits per pixel,
     * scanline stride and data bit offset.
     *
     * @param dataType      the data type for storing samples
     * @param w             the width, in pixels, of the region of
     *                      image data described
     * @param h             the height, in pixels, of the region of
     *                      image data described
     * @param numberOfBits  the number of bits per pixel
     * @param dataBitOffset the data bit offset for the region of image
     *                      data described
     * @throws RasterFormatException    if the number of bits per pixel
     *                                  is not a power of 2 or if a power of 2 number of
     *                                  pixels do not fit in one data element.
     * @throws IllegalArgumentException if <code>w</code> or
     *                                  <code>h</code> is not greater than 0
     * @throws IllegalArgumentException if <code>dataType</code> is not
     *                                  either <code>DataBuffer.TYPE_BYTE</code>,
     *                                  <code>DataBuffer.TYPE_USHORT</code>, or
     *                                  <code>DataBuffer.TYPE_INT</code>
     */
    protected VerticalMultiPixelPackedSampleModel(int dataType, int w, int h,
                                                  int numberOfBits,
                                                  int dataBitOffset) {
        super(dataType, w, h, 1);
        if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_USHORT &&
                dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("Unsupported data type " +
                    dataType);
        }
        //todo: Testing dataBits
        if (dataBitOffset != 0) {
            LOGGER.warn("A nonzero dataBitOffset is currently untested. Errors are likely");
        }
        this.dataType = dataType;
        this.pixelBitStride = numberOfBits;
        this.dataBitOffset = dataBitOffset;
        this.dataElementSize = DataBuffer.getDataTypeSize(dataType);
        this.pixelsPerDataElement = dataElementSize / numberOfBits;
        if (pixelsPerDataElement * numberOfBits != dataElementSize) {
            throw new RasterFormatException("MultiPixelPackedSampleModel " +
                    "does not allow pixels to " +
                    "span data element boundaries");
        }
        this.bitMask = (1 << numberOfBits) - 1;
    }

    /**
     * This factory method creates a Writable raster which encapsulates a newly created <code>VerticalMultiPixelPackedSampleModel</code>
     *
     * @param dataBuffer   the <code>DataBuffer</code> object used to create a <code>VerticalMultiPixelPackedSampleModel</code>
     * @param w            the width of the raster
     * @param h            the height of the raster
     * @param bitsPerPixel the length of each pixel in bits
     * @param location     the Point in dataBuffer which represents (0, 0) for the raster
     * @return
     */
    public static WritableRaster createRaster(DataBuffer dataBuffer,
                                              int w, int h,
                                              int bitsPerPixel,
                                              Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        int dataType = dataBuffer.getDataType();

        if (dataType != DataBuffer.TYPE_BYTE &&
                dataType != DataBuffer.TYPE_USHORT &&
                dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("Unsupported data type " +
                    dataType);
        }

        if (dataBuffer.getNumBanks() != 1) {
            throw new
                    RasterFormatException("DataBuffer for packed Rasters" +
                    " must only have 1 bank.");
        }

        VerticalMultiPixelPackedSampleModel vmppsm =
                new VerticalMultiPixelPackedSampleModel(dataType, w, h, bitsPerPixel);

        return new OLEDRaster(vmppsm, new Point());
    }

    /**
     * Creates a new <code>VerticalMultiPixelPackedSampleModel</code> with the
     * specified width and height.  The new
     * <code>VerticalMultiPixelPackedSampleModel</code> has the
     * same storage data type and number of bits per pixel as this
     * <code>VerticalMultiPixelPackedSampleModel</code>.
     *
     * @param w the specified width
     * @param h the specified height
     * @return a {@link SampleModel} with the specified width and height
     * and with the same storage data type and number of bits per pixel
     * as this <code>VerticalMultiPixelPackedSampleModel</code>.
     * @throws IllegalArgumentException if <code>w</code> or
     *                                  <code>h</code> is not greater than 0
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        SampleModel sampleModel =
                new java.awt.image.MultiPixelPackedSampleModel(dataType, w, h, pixelBitStride);
        return sampleModel;
    }

    /**
     * Creates a <code>DataBuffer</code> that corresponds to this
     * <code>VerticalMultiPixelPackedSampleModel</code>.  The
     * <code>DataBuffer</code> object's data type and size
     * is consistent with this <code>MultiPixelPackedSampleModel</code>.
     * The <code>DataBuffer</code> has a single bank.
     *
     * @return a <code>DataBuffer</code> with the same data type and
     * size as this <code>VerticalMultiPixelPackedSampleModel</code>.
     */
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;

        int size = width * height * pixelBitStride / pixelsPerDataElement;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                dataBuffer = new DataBufferByte(size + (dataBitOffset + 7) / 8);
                break;
            case DataBuffer.TYPE_USHORT:
                dataBuffer = new DataBufferUShort(size + (dataBitOffset + 15) / 16);
                break;
            case DataBuffer.TYPE_INT:
                dataBuffer = new DataBufferInt(size + (dataBitOffset + 31) / 32);
                break;
        }
        return dataBuffer;
    }

    /**
     * Returns the number of data elements needed to transfer one pixel
     * via the {@link #getDataElements} and {@link #setDataElements}
     * methods.  For a <code>VerticalMultiPixelPackedSampleModel</code>, this is
     * one.
     *
     * @return the number of data elements. (1)
     */
    public int getNumDataElements() {
        return 1;
    }

    /**
     * Returns the number of bits per sample for all bands.
     *
     * @return the number of bits per sample.
     */
    public int[] getSampleSize() {
        int sampleSize[] = {pixelBitStride};
        return sampleSize;
    }

    /**
     * Returns the number of bits per sample for the specified band.
     *
     * @param band the specified band
     * @return the number of bits per sample for the specified band.
     */
    public int getSampleSize(int band) {
        return pixelBitStride;
    }

    /**
     * Returns the offset of pixel (x,&nbsp;y) in data array elements.
     *
     * @param x the X coordinate of the specified pixel
     * @param y the Y coordinate of the specified pixel
     * @return the offset of the specified pixel.
     */
    public int getOffset(int x, int y) {
        int offset = y / pixelsPerDataElement * width;
        offset += x;
        offset += (y % pixelsPerDataElement + dataBitOffset) / dataElementSize;
        return offset;
    }

    /**
     * Returns the pixel bit stride in bits.  This value is
     * the number of bits per pixel.
     *
     * @return the <code>pixelBitStride</code> of this
     * <code>VerticalMultiPixelPackedSampleModel</code>.
     */
    public int getPixelBitStride() {
        return pixelBitStride;
    }

    /**
     * Returns the data bit offset in bits.
     *
     * @return the <code>dataBitOffset</code> of this
     * <code>VerticalMultiPixelPackedSampleModel</code>.
     */
    public int getDataBitOffset() {
        return dataBitOffset;
    }

    /**
     * Returns the TransferType used to transfer pixels by way of the
     * <code>getDataElements</code> and <code>setDataElements</code>
     * methods. The TransferType might or might not be the same as the
     * storage DataType.  The TransferType is one of
     * DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT,
     * or DataBuffer.TYPE_INT.
     *
     * @return the transfertype.
     */
    public int getTransferType() {
        if (pixelBitStride > 16)
            return DataBuffer.TYPE_INT;
        else if (pixelBitStride > 8)
            return DataBuffer.TYPE_USHORT;
        else
            return DataBuffer.TYPE_BYTE;
    }

    /**
     * Creates a new <code>MultiPixelPackedSampleModel</code> with a
     * subset of the bands of this
     * <code>MultiPixelPackedSampleModel</code>.  Since a
     * <code>MultiPixelPackedSampleModel</code> only has one band, the
     * bands argument must have a length of one and indicate the zeroth
     * band.
     *
     * @param bands the specified bands
     * @return a new <code>SampleModel</code> with a subset of bands of
     * this <code>MultiPixelPackedSampleModel</code>.
     * @throws RasterFormatException    if the number of bands requested
     *                                  is not one.
     * @throws IllegalArgumentException if <code>w</code> or
     *                                  <code>h</code> is not greater than 0
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        if (bands != null) {
            if (bands.length != 1)
                throw new RasterFormatException("VerticalMultiPixelPackedSampleModel has "
                        + "only one band.");
        }
        return createCompatibleSampleModel(width, height);
    }

    /**
     * Returns as <code>int</code> the sample in a specified band for the
     * pixel located at (x,&nbsp;y).  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if the
     * coordinates are not in bounds.
     *
     * @param x    the X coordinate of the specified pixel
     * @param y    the Y coordinate of the specified pixel
     * @param b    the band to return, which is assumed to be 0
     * @param data the <code>DataBuffer</code> containing the image
     *             data
     * @return the specified band containing the sample of the specified
     * pixel.
     * @throws ArrayIndexOutOfBoundsException if the specified
     *                                        coordinates are not in bounds.
     * @see #setSample(int, int, int, int, DataBuffer)
     */
    public int getSample(int x, int y, int b, DataBuffer data) {
        // 'b' must be 0
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height) ||
                (b != 0)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int element = data.getElem(getOffset(x, y));
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        return (element >> shift) & bitMask;
    }

    /**
     * Sets a sample in the specified band for the pixel located at
     * (x,&nbsp;y) in the <code>DataBuffer</code> using an
     * <code>int</code> for input.
     * An <code>ArrayIndexOutOfBoundsException</code> is thrown if the
     * coordinates are not in bounds.
     *
     * @param x    the X coordinate of the specified pixel
     * @param y    the Y coordinate of the specified pixel
     * @param b    the band to return, which is assumed to be 0
     * @param s    the input sample as an <code>int</code>
     * @param data the <code>DataBuffer</code> where image data is stored
     * @throws ArrayIndexOutOfBoundsException if the coordinates are
     *                                        not in bounds.
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b, int s,
                          DataBuffer data) {
        // 'b' must be 0
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height) ||
                (b != 0)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int index = getOffset(x, y);
        int element = data.getElem(index);
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        element &= ~(bitMask << shift);
        element |= (s & bitMask) << shift;
        data.setElem(index, element);
    }

    /**
     * Returns data for a single pixel in a primitive array of type
     * TransferType.  For a <code>VerticalMultiPixelPackedSampleModel</code>,
     * the array has one element, and the type is the smallest of
     * DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT
     * that can hold a single pixel.  Generally, <code>obj</code>
     * should be passed in as <code>null</code>, so that the
     * <code>Object</code> is created automatically and is the
     * correct primitive data type.
     * <p>
     * The following code illustrates transferring data for one pixel from
     * <code>DataBuffer</code> <code>db1</code>, whose storage layout is
     * described by <code>VerticalMultiPixelPackedSampleModel</code>
     * <code>vmppsm1</code>, to <code>DataBuffer</code> <code>db2</code>,
     * whose storage layout is described by
     * <code>VerticalMultiPixelPackedSampleModel</code> <code>vmppsm2</code>.
     * The transfer is generally more efficient than using
     * <code>getPixel</code> or <code>setPixel</code>.
     * <pre>
     *       VerticalMultiPixelPackedSampleModel vmppsm1, vmppsm2;
     *       DataBufferInt db1, db2;
     *       vmppsm2.setDataElements(x, y, vmppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * Using <code>getDataElements</code> or <code>setDataElements</code>
     * to transfer between two <code>DataBuffer/SampleModel</code> pairs
     * is legitimate if the <code>SampleModels</code> have the same number
     * of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * If <code>obj</code> is not <code>null</code>, it should be a
     * primitive array of type TransferType.  Otherwise, a
     * <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if the
     * coordinates are not in bounds, or if <code>obj</code> is not
     * <code>null</code> and is not large enough to hold the pixel data.
     *
     * @param x    the X coordinate of the specified pixel
     * @param y    the Y coordinate of the specified pixel
     * @param obj  a primitive array in which to return the pixel data or
     *             <code>null</code>.
     * @param data the <code>DataBuffer</code> containing the image data.
     * @return an <code>Object</code> containing data for the specified
     * pixel.
     * @throws ClassCastException             if <code>obj</code> is not a
     *                                        primitive array of type TransferType or is not <code>null</code>
     * @throws ArrayIndexOutOfBoundsException if the coordinates are
     *                                        not in bounds, or if <code>obj</code> is not <code>null</code> or
     *                                        not large enough to hold the pixel data
     * @see #setDataElements(int, int, Object, DataBuffer)
     */
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }

        int type = getTransferType();
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        int element = 0;

        switch (type) {

            case DataBuffer.TYPE_BYTE:

                byte[] bdata;

                if (obj == null)
                    bdata = new byte[1];
                else
                    bdata = (byte[]) obj;

                element = data.getElem(getOffset(x, y));
                bdata[0] = (byte) ((element >> shift) & bitMask);

                obj = bdata;
                break;

            case DataBuffer.TYPE_USHORT:

                short[] sdata;

                if (obj == null)
                    sdata = new short[1];
                else
                    sdata = (short[]) obj;

                element = data.getElem(getOffset(x, y));
                sdata[0] = (short) ((element >> shift) & bitMask);

                obj = sdata;
                break;

            case DataBuffer.TYPE_INT:

                int[] idata;

                if (obj == null)
                    idata = new int[1];
                else
                    idata = (int[]) obj;

                element = data.getElem(getOffset(x, y));
                idata[0] = (element >> shift) & bitMask;

                obj = idata;
                break;
        }

        return obj;
    }

    /**
     * Returns the specified single band pixel in the first element
     * of an <code>int</code> array.
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if the
     * coordinates are not in bounds.
     *
     * @param x      the X coordinate of the specified pixel
     * @param y      the Y coordinate of the specified pixel
     * @param iArray the array containing the pixel to be returned or
     *               <code>null</code>
     * @param data   the <code>DataBuffer</code> where image data is stored
     * @return an array containing the specified pixel.
     * @throws ArrayIndexOutOfBoundsException if the coordinates
     *                                        are not in bounds
     * @see #setPixel(int, int, int[], DataBuffer)
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray != null) {
            pixels = iArray;
        } else {
            pixels = new int[numBands];
        }
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        int element = data.getElem(getOffset(x, y));
        pixels[0] = (element >> shift) & bitMask;
        return pixels;
    }

    /**
     * Sets the data for a single pixel in the specified
     * <code>DataBuffer</code> from a primitive array of type
     * TransferType.  For a <code>VerticalMultiPixelPackedSampleModel</code>,
     * only the first element of the array holds valid data,
     * and the type must be the smallest of
     * DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT
     * that can hold a single pixel.
     * <p>
     * The following code illustrates transferring data for one pixel from
     * <code>DataBuffer</code> <code>db1</code>, whose storage layout is
     * described by <code>VerticalMultiPixelPackedSampleModel</code>
     * <code>vmppsm1</code>, to <code>DataBuffer</code> <code>db2</code>,
     * whose storage layout is described by
     * <code>VerticalMultiPixelPackedSampleModel</code> <code>vmppsm2</code>.
     * The transfer is generally more efficient than using
     * <code>getPixel</code> or <code>setPixel</code>.
     * <pre>
     *       VerticalMultiPixelPackedSampleModel vmppsm1, vmppsm2;
     *       DataBufferInt db1, db2;
     *       vmppsm2.setDataElements(x, y, vmppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * Using <code>getDataElements</code> or <code>setDataElements</code> to
     * transfer between two <code>DataBuffer/SampleModel</code> pairs is
     * legitimate if the <code>SampleModel</code> objects have
     * the same number of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * <code>obj</code> must be a primitive array of type TransferType.
     * Otherwise, a <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if the
     * coordinates are not in bounds, or if <code>obj</code> is not large
     * enough to hold the pixel data.
     *
     * @param x    the X coordinate of the pixel location
     * @param y    the Y coordinate of the pixel location
     * @param obj  a primitive array containing pixel data
     * @param data the <code>DataBuffer</code> containing the image data
     * @see #getDataElements(int, int, Object, DataBuffer)
     */
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int type = getTransferType();
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        int index = getOffset(x, y);
        int element = data.getElem(index);
        element &= ~(bitMask << shift);

        switch (type) {

            case DataBuffer.TYPE_BYTE:

                byte[] barray = (byte[]) obj;
                element |= (((int) (barray[0]) & 0xff) & bitMask) << shift;
                data.setElem(index, element);
                break;

            case DataBuffer.TYPE_USHORT:

                short[] sarray = (short[]) obj;
                element |= (((int) (sarray[0]) & 0xffff) & bitMask) << shift;
                data.setElem(index, element);
                break;

            case DataBuffer.TYPE_INT:

                int[] iarray = (int[]) obj;
                element |= (iarray[0] & bitMask) << shift;
                data.setElem(index, element);
                break;
        }
    }

    /**
     * Sets a pixel in the <code>DataBuffer</code> using an
     * <code>int</code> array for input.
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if
     * the coordinates are not in bounds.
     *
     * @param x      the X coordinate of the pixel location
     * @param y      the Y coordinate of the pixel location
     * @param iArray the input pixel in an <code>int</code> array
     * @param data   the <code>DataBuffer</code> containing the image data
     * @see #getPixel(int, int, int[], DataBuffer)
     */
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int shift = (y % pixelsPerDataElement + dataBitOffset) & (dataElementSize - 1);
        int index = getOffset(x, y);
        int element = data.getElem(index);
        element &= ~(bitMask << shift);
        element |= (iArray[0] & bitMask) << shift;
        data.setElem(index, element);
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof VerticalMultiPixelPackedSampleModel)) {
            return false;
        }

        VerticalMultiPixelPackedSampleModel that = (VerticalMultiPixelPackedSampleModel) o;
        return this.width == that.width &&
                this.height == that.height &&
                this.numBands == that.numBands &&
                this.dataType == that.dataType &&
                this.pixelBitStride == that.pixelBitStride &&
                this.bitMask == that.bitMask &&
                this.pixelsPerDataElement == that.pixelsPerDataElement &&
                this.dataElementSize == that.dataElementSize &&
                this.dataBitOffset == that.dataBitOffset;
    }

    // If we implement equals() we must also implement hashCode
    public int hashCode() {
        int hash = 0;
        hash = width;
        hash <<= 8;
        hash ^= height;
        hash <<= 8;
        hash ^= numBands;
        hash <<= 8;
        hash ^= dataType;
        hash <<= 8;
        hash ^= pixelBitStride;
        hash <<= 8;
        hash ^= bitMask;
        hash <<= 8;
        hash ^= pixelsPerDataElement;
        hash <<= 8;
        hash ^= dataElementSize;
        hash <<= 8;
        hash ^= dataBitOffset;
        return hash;
    }
}