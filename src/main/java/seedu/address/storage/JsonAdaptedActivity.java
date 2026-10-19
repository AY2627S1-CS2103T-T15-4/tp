package seedu.address.storage;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Jackson-friendly schedule; dog membership is not part of a repeating activity. */
class JsonAdaptedActivity {
    private final Integer id;
    private final String name;
    private final String startDate;
    private final String startTime;
    private final String endTime;
    private final String recurrence;
    private final Boolean completed;

    /** Constructs an adapter from persisted fields. All fields are required. */
    public JsonAdaptedActivity(Integer id, String name, String startDate, String startTime,
            String endTime, String recurrence) {
        this(id, name, startDate, startTime, endTime, recurrence, false);
    }

    /** Constructs an adapter including the completion state. */
    @JsonCreator
    public JsonAdaptedActivity(
            @JsonProperty("id") @JsonDeserialize(using = ActivityIdDeserializer.class) Integer id,
            @JsonProperty("name") String name, @JsonProperty("startDate") String startDate,
            @JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
            @JsonProperty("recurrence") String recurrence, @JsonProperty("completed") Boolean completed) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrence = recurrence;
        this.completed = completed;
    }

    /** Takes a snapshot of a stored schedule. */
    public JsonAdaptedActivity(Activity source) {
        id = source.getId();
        name = source.getName().toString();
        startDate = source.getStartDate().toString();
        startTime = source.getStartTime().toString();
        endTime = source.getEndTime().toString();
        recurrence = source.getRecurrence().name();
        completed = source.isCompleted();
    }

    /** Restores a schedule, rejecting missing fields or invalid domain values. */
    public Activity toModelType() throws IllegalValueException {
        if (id == null || name == null || startDate == null || startTime == null
                || endTime == null || recurrence == null) {
            throw new IllegalValueException("Activity fields must all be present.");
        }
        try {
            return new Activity(id, new ActivityName(name), LocalDate.parse(startDate), LocalTime.parse(startTime),
                    LocalTime.parse(endTime), Recurrence.valueOf(recurrence), completed != null && completed);
        } catch (DateTimeException | IllegalArgumentException e) {
            throw new IllegalValueException("Invalid activity: " + e.getMessage(), e);
        }
    }
}
