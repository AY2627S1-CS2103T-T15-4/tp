package seedu.address.model.dog.exceptions;

/** Indicates that dogs cannot be enrolled in an activity due to invalid enrolment data. */
public class EnrolException extends Exception {
    /** Creates an exception with the specified explanation. */
    public EnrolException(String message) {
        super(message);
    }
}
