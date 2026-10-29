// @@author Kenneth-Chia
package seedu.address.model.dog.exceptions;

/** Indicates that a batch of dogs cannot be linked without violating ownership rules. */
public class LinkException extends Exception {
    public LinkException(String message) {
        super(message);
    }
}
