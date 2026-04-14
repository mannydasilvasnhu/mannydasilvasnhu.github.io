package com.example.powerscale.utils;

/**
 * Event<T>
 * This wrapper class is used for one-time LiveData events so values are only handled once.
 *
 * @param <T> - The type of content wrapped by the event.
 */
public class Event<T> {

    private final T content;
    private boolean hasBeenHandled = false;

    /**
     * Event(T content)
     * Creates a new one-time event wrapper.
     *
     * @param content - The value to wrap.
     */
    public Event(T content) {
        this.content = content;
    }

    /**
     * getContentIfNotHandled()
     * Returns the content only if it has not already been consumed.
     *
     * @return T - The wrapped content, or null if already handled.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        }

        hasBeenHandled = true;
        return content;
    }

    /**
     * peekContent()
     * Returns the content without marking it handled.
     *
     * This method is currently not utilized by the application,
     * but will be kept here for potential future use.
     *
     * @return T - The wrapped content.
     */
    public T peekContent() {
        return content;
    }
}