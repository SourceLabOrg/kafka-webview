package org.sourcelab.kafka.webview.ui.manager.ui;

/**
 * Represents an Alert Message.
 */
public class FlashMessage {
    private final String type;
    private final String message;

    public FlashMessage(final String type, final String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public static FlashMessage newSuccess(final String message) {
        return new FlashMessage("success", message);
    }

    public static FlashMessage newInfo(final String message) {
        return new FlashMessage("info", message);
    }

    public static FlashMessage newWarning(final String message) {
        return new FlashMessage("warning", message);
    }

    public static FlashMessage newDanger(final String message) {
        return new FlashMessage("danger", message);
    }
}
