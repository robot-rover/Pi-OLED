package de.pi3g.pi.oled;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * This raster is used in conjunction with a {@link VerticalMultiPixelPackedSampleModel} to create an imagebuffer for the display that is accessible by Java java.awt.Graphics
 */
public class OLEDRaster extends WritableRaster {
    protected OLEDRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        super(sampleModel, dataBuffer, origin);
        if (!(sampleModel instanceof VerticalMultiPixelPackedSampleModel)) {
            throw new UnsupportedOperationException("OLEDRaster can only be created using a VerticalMultiPixelPackedSampleModel");
        }
    }
}
