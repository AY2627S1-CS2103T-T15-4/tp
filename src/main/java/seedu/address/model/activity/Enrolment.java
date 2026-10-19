package seedu.address.model.activity;

import java.util.Objects;

/** Represents the enrolment of one dog in one activity. */
public class Enrolment {
    private final int activityId;
    private final int dogId;

    /** Creates an enrolment record. */
    public Enrolment(int activityId, int dogId) {
        if (activityId < 0 || dogId < 0) {
            throw new IllegalArgumentException("Enrolment IDs must be nonnegative.");
        }
        this.activityId = activityId;
        this.dogId = dogId;
    }

    /** Returns the activity ID. */
    public int getActivityId() {
        return activityId;
    }

    /** Returns the dog ID. */
    public int getDogId() {
        return dogId;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Enrolment otherEnrolment
                && activityId == otherEnrolment.activityId && dogId == otherEnrolment.dogId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, dogId);
    }
}
