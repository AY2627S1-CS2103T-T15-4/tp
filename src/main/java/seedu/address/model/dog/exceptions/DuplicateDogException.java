package seedu.address.model.dog.exceptions;

/** Signals that an operation would result in duplicate dogs. */
public class DuplicateDogException extends RuntimeException {
    public DuplicateDogException() {
        super("Operation would result in duplicate dogs");
    }
}
