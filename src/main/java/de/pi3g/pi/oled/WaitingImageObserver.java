package de.pi3g.pi.oled;

/*
 * JCommon : a free general purpose class library for the Java(tm) platform
 *
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -------------------------
 * WaitingImageObserver.java
 * -------------------------
 * (C)opyright 2000-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner
 * Contributor(s):   Stefan Prange;
 *
 * $Id: WaitingImageObserver.java,v 1.8 2008/09/10 09:24:41 mungady Exp $
 *
 * Changes (from 8-Feb-2002)
 * -------------------------
 * 15-Apr-2002 : first version used by ImageElement.
 * 16-May-2002 : Line delimiters adjusted
 * 04-Jun-2002 : Documentation and added a NullPointerCheck for the constructor.
 * 14-Jul-2002 : BugFixed: WaitingImageObserver dead-locked (bugfix by Stefan
 *               Prange)
 * 18-Mar-2003 : Updated header and made minor Javadoc changes (DG);
 * 21-Sep-2003 : Moved from JFreeReport.
 */


import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * This image observer blocks until the image is completely loaded. AWT
 * defers the loading of images until they are painted on a graphic.
 * <p>
 * While printing reports it is not very nice, not to know whether a image
 * was completely loaded, so this observer forces the loading of the image
 * until a final state (either ALLBITS, ABORT or ERROR) is reached.
 *
 * @author Thomas Morgner
 */
public class WaitingImageObserver implements ImageObserver {

    /**
     * The lock.
     */
    private boolean lock;

    /**
     * A flag that signals an error.
     */
    private boolean error;

    /**
     * Creates a new <code>ImageObserver</code> for the given <code>Image</code>.
     * The observer has to be started by an external thread.
     */
    public WaitingImageObserver() {
        this.lock = true;
    }

    /**
     * Callback function used by AWT to inform that more data is available. The
     * observer waits until either all data is loaded or AWT signals that the
     * image cannot be loaded.
     *
     * @param img       the image being observed.
     * @param infoflags the bitwise inclusive OR of the following
     *                  flags:  <code>WIDTH</code>, <code>HEIGHT</code>,
     *                  <code>PROPERTIES</code>, <code>SOMEBITS</code>,
     *                  <code>FRAMEBITS</code>, <code>ALLBITS</code>,
     *                  <code>ERROR</code>, <code>ABORT</code>.
     * @param x         the <i>x</i> coordinate.
     * @param y         the <i>y</i> coordinate.
     * @param width     the width.
     * @param height    the height.
     * @return <code>false</code> if the infoflags indicate that the
     * image is completely loaded; <code>true</code> otherwise.
     */
    public synchronized boolean imageUpdate(
            final Image img,
            final int infoflags,
            final int x,
            final int y,
            final int width,
            final int height) {
        System.out.println("Image update!");
        if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
            this.lock = false;
            this.error = false;
            notifyAll();
            return false;
        } else if ((infoflags & ImageObserver.ABORT) == ImageObserver.ABORT
                || (infoflags & ImageObserver.ERROR) == ImageObserver.ERROR) {
            this.lock = false;
            this.error = true;
            notifyAll();
            return false;
        }
        //notifyAll();
        return true;
    }

    /**
     * This method waits for the AWT to load the image.
     * <p>
     * It should be noted that if the original awt operation returned true,
     * awt will never call the Observer and this method will block indefinitely
     */
    public synchronized void waitImageLoaded() {
        while (this.lock) {
            try {
                System.out.println("Waiting");
                wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks to see if the image has finished loading
     *
     * @return if the loading is complete
     */
    public boolean isLoadingComplete() {
        return !lock;
    }

    /**
     * Checks to see if an error has been encountered loading the image
     *
     * @return if an error was encountered
     */
    public boolean isError() {
        return this.error;
    }
}