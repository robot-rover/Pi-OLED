package de.pi3g.pi.oled.buttons;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is for use with the Adafruit 128x64 OLED Bonnet
 * (<a href="https://www.adafruit.com/product/3531">https://www.adafruit.com/product/3531</a>).
 * It provides easy access to all of the panel's buttons.
 * An interface for both directly reading the buttons and
 * adding event listeners for the pins are provided.
 *
 * @see Button
 */
public class OLEDButtons {

    private final Map<Button, GpioPinDigitalInput> buttonMap;
    private final GpioController controller;

    /**
     * This creates a new <code>OLEDButtons</code> instance
     */
    public OLEDButtons() {
        controller = GpioFactory.getInstance();
        buttonMap = new HashMap<Button, GpioPinDigitalInput>(10);
        for (Button button : Button.values()) {
            buttonMap.put(button, controller.provisionDigitalInputPin(button.source, PinPullResistance.PULL_UP));
        }
    }

    /**
     * Gets the current state of the Left Button
     *
     * @return if the button is pressed down
     */
    public boolean getLeft() {
        return getDirection(Button.LEFT);
    }

    /**
     * Gets the current state of the Right Button
     *
     * @return if the button is pressed down
     */
    public boolean getRight() {
        return getDirection(Button.RIGHT);
    }

    /**
     * Gets the current state of the Up Button
     *
     * @return if the button is pressed down
     */
    public boolean getUp() {
        return getDirection(Button.UP);
    }

    /**
     * Gets the current state of the Down Button
     *
     * @return if the button is pressed down
     */
    public boolean getDown() {
        return getDirection(Button.DOWN);
    }

    /**
     * Gets the current state of the Center Button
     *
     * @return if the button is pressed down
     */
    public boolean getCenter() {
        return getDirection(Button.CENTER);
    }

    /**
     * Gets the current state of the A Button
     *
     * @return if the button is pressed down
     */
    public boolean getAButton() {
        return getDirection(Button.A_BUTTON);
    }

    /**
     * Gets the current state of the B Button
     *
     * @return if the button is pressed down
     */
    public boolean getBButton() {
        return getDirection(Button.B_BUTTON);
    }

    /**
     * Gets the current state of a Button
     *
     * @param direction The direction to check
     * @return if the button is pressed down
     */
    public boolean getDirection(Button direction) {
        return buttonMap.get(direction).getState().isLow();
    }

    /**
     * Shuts down the GPIO Controller
     */
    public void shutdown() {
        controller.shutdown();
    }

    /**
     * Adds a listener that is notified whenever a button is pressed or released
     *
     * @param listener the listener to notify
     */
    public void addButtonListner(ButtonListener listener) {
        EventAdapter adapter = new EventAdapter(listener);
        for (Map.Entry<Button, GpioPinDigitalInput> entry : buttonMap.entrySet()) {
            entry.getValue().addListener(adapter);
        }
    }

    /**
     * Removes a listener that was previously added so it will no longer be called
     *
     * @param listener The listener to remove
     */
    public void removeButtonListner(ButtonListener listener) {
        for (Map.Entry<Button, GpioPinDigitalInput> entry : buttonMap.entrySet()) {
            for (GpioPinListener listener1 : entry.getValue().getListeners()) {
                if (listener1 instanceof EventAdapter)
                    if (((EventAdapter) listener1).listener == listener) {
                        entry.getValue().removeListener(listener1);
                        break;
                    }
            }
        }
    }

    /**
     * The different buttons supported by the <code>OLEDButtons</code> class.
     */
    public enum Button {
        /**
         * Pushing the joystick up
         */
        UP(RaspiPin.GPIO_00),

        /**
         * Pushing the joystick down
         */
        DOWN(RaspiPin.GPIO_03),

        /**
         * Pushing the joystick left
         */
        LEFT(RaspiPin.GPIO_02),

        /**
         * Pushing the joystick right
         */
        RIGHT(RaspiPin.GPIO_04),

        /**
         * Pushing the joystick in
         */
        CENTER(RaspiPin.GPIO_07),

        /**
         * The bottom left button to the right of the display
         */
        A_BUTTON(RaspiPin.GPIO_21),

        /**
         * The top right button to the right of the display
         */
        B_BUTTON(RaspiPin.GPIO_22);

        private Pin source;

        Button(Pin source) {
            this.source = source;
        }

        /**
         * Given a Pi4J Pin, this method finds a button associated with the pin
         *
         * @param pin The Pi4J Pin
         * @return The button associated with the pin, or null if none exist
         */
        public static Button getButtonFromPin(Pin pin) {
            for (Button button : Button.values()) {
                if (button.source.getAddress() == pin.getAddress())
                    return button;
            }
            return null;
        }

        /**
         * Gets the Pi4J Pin associated with this Button
         *
         * @return The Pi4J Pin
         */
        public Pin getPin() {
            return source;
        }
    }

    class EventAdapter implements GpioPinListenerDigital {

        ButtonListener listener;

        EventAdapter(ButtonListener listener) {
            this.listener = listener;
        }

        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            Button button = Button.getButtonFromPin(event.getPin().getPin());
            boolean pressed = event.getState().isLow();
            listener.handleButtonEvent(new ButtonEvent(event.getSource(), pressed, button));
        }
    }


}
