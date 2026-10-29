// @@author Kenneth-Chia
package seedu.address.model.dog.exceptions;

/** Indicates that a batch cannot be unlinked from the specified customer. */
public class UnlinkException extends Exception {
    public UnlinkException(String message) {
        super(message);
    }
}
// @@author
