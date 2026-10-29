package seedu.address.model.activity;

import java.time.LocalDate;
import java.util.Objects;

/** Represents the enrolment of one dog in one occurrence of an activity. */
public class Enrolment {
    private final int activityId;
    private final int dogId;
    private final LocalDate date;

    /**
     * Creates an enrolment record with positive persistent activity and dog IDs.
     *
     * @throws IllegalArgumentException If either ID is not positive.
     */
    public Enrolment(int activityId, int dogId, LocalDate date) {
        Objects.requireNonNull(date);
        if (activityId < 1 || dogId < 1) {
            throw new IllegalArgumentException("Activity and dog IDs must be positive.");
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
