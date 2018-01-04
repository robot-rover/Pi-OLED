package de.pi3g.pi.oled.buttons;

import java.util.EventObject;

/**
 * This object is used to represent changing button states to {@link ButtonListener} objects
 */
public class ButtonEvent extends EventObject {
    private final OLEDButtons.Button button;
    private final boolean pressed;

    /**
     * Constructs a button event with the given source object.
     * This object will store the button and its state
     *
     * @param obj     The source of this event
     * @param pressed The current state of the button (True is pushed down)
     * @param button  The button that changed states
     */
    ButtonEvent(Object obj, boolean pressed, OLEDButtons.Button button) {
        super(obj);
        this.pressed = pressed;
        this.button = button;
    }

    /**
     * Gets which Button this event is for
     *
     * @return The button that changed states
     */
    public OLEDButtons.Button getButton() {
        return button;
    }

    /**
     * Gets what the new state of the button is
     *
     * @return The new state of the button (True is pushed down)
     */
    public boolean isPressed() {
        return pressed;
    }
}
