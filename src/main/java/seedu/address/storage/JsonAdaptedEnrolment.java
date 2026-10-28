package seedu.address.storage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.activity.Enrolment;

/** Jackson-friendly version of {@link Enrolment}. */
class JsonAdaptedEnrolment {
    private final Integer activityId;
    private final Integer dogId;
    private final LocalDate date;

    @JsonCreator
    public JsonAdaptedEnrolment(@JsonProperty("activityId") Integer activityId,
            @JsonProperty("dogId") Integer dogId, @JsonProperty("date") LocalDate date) {
        this.activityId = activityId;
        this.dogId = dogId;
        this.date = date;
    }

    /** Creates a legacy adapter for data written before enrolments had occurrence dates. */
    public JsonAdaptedEnrolment(Integer activityId, Integer dogId) {
        this(activityId, dogId, null);
    }

    /** Creates an adapter from an enrolment. */
    public JsonAdaptedEnrolment(Enrolment source) {
        activityId = source.getActivityId();
        dogId = source.getDogId();
        date = source.getDate();
    }

    /** Returns the persisted activity ID, if present. */
    public Integer getActivityId() {
        return activityId;
    }

    /** Converts this adapter to an enrolment. */
    public Enrolment toModelType() throws IllegalValueException {
        return toModelType(null);
    }

    /** Converts this adapter, migrating a legacy record to the supplied activity start date when necessary. */
    public Enrolment toModelType(LocalDate legacyDate) throws IllegalValueException {
        if (activityId == null || dogId == null || activityId < 1 || dogId < 1) {
            throw new IllegalValueException("Activity and dog IDs must be positive.");
        }
        LocalDate resolvedDate = date == null ? legacyDate : date;
        if (resolvedDate == null) {
            throw new IllegalValueException("Enrolment date must be present.");
        }
        return new Enrolment(activityId, dogId, resolvedDate);
    }
}
