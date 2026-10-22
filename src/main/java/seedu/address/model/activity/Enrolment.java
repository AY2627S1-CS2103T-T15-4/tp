package seedu.address.model.activity;

import java.time.LocalDate;
import java.util.Objects;

/** Represents the enrolment of one dog in one occurrence of an activity. */
public class Enrolment {
    private final int activityId;
    private final int dogId;
    private final LocalDate date;

    /** Creates an enrolment record. */
    public Enrolment(int activityId, int dogId, LocalDate date) {
        Objects.requireNonNull(date);
        if (activityId < 0 || dogId < 0) {
            throw new IllegalArgumentException("Enrolment IDs must be nonnegative.");
        }
        this.activityId = activityId;
        this.dogId = dogId;
        this.date = date;
    }

    /** Returns the activity ID. */
    public int getActivityId() {
        return activityId;
    }

    /** Returns the dog ID. */
    public int getDogId() {
        return dogId;
    }

    /** Returns the date of the activity occurrence. */
    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Enrolment otherEnrolment
                && activityId == otherEnrolment.activityId && dogId == otherEnrolment.dogId
                && date.equals(otherEnrolment.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, dogId, date);
    }
}
