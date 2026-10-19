package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.activity.Enrolment;

/** Jackson-friendly version of {@link Enrolment}. */
class JsonAdaptedEnrolment {
    private final Integer activityId;
    private final Integer dogId;

    @JsonCreator
    public JsonAdaptedEnrolment(@JsonProperty("activityId") Integer activityId,
            @JsonProperty("dogId") Integer dogId) {
        this.activityId = activityId;
        this.dogId = dogId;
    }

    /** Creates an adapter from an enrolment. */
    public JsonAdaptedEnrolment(Enrolment source) {
        activityId = source.getActivityId();
        dogId = source.getDogId();
    }

    /** Converts this adapter to an enrolment. */
    public Enrolment toModelType() throws IllegalValueException {
        if (activityId == null || dogId == null || activityId < 0 || dogId < 0) {
            throw new IllegalValueException("Enrolment IDs must be nonnegative and present.");
        }
        return new Enrolment(activityId, dogId);
    }
}
