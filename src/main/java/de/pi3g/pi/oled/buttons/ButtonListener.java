package de.pi3g.pi.oled.buttons;

import java.util.EventListener;

/**
 * This interface represents a listener for button events.
 * When registered using {@link OLEDButtons#addButtonListner(ButtonListener)},
 * this listener will be notified when any of the panel's buttons are pushed down or released.
 */
public interface ButtonListener extends EventListener {
    /**
     * When this listener is registered, this method is called every time a button changes state.
     *
     * @param event A <code>ButtonEvent</code> representing a changed button state.
     */
    void handleButtonEvent(ButtonEvent event);
}
