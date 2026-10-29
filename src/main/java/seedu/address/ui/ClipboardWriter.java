package seedu.address.ui;

/** Writes plain text to a clipboard destination and reports whether the write succeeded. */
@FunctionalInterface
interface ClipboardWriter {

    /**
     * Writes {@code value} to the clipboard destination.
     *
     * @return True if the value was written successfully.
     */
    boolean write(String value);
}
