package seedu.address.model.activity;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An immutable activity schedule with a persistent, nonnegative ID.
 * Times are local daycare times on the same date; the end must be later than the start.
 * Dogs belong to separately stored dated bookings, never to this repeating schedule.
 * This class does not allocate IDs, validate booking conflicts, or access the clock.
 */
public final class Activity {
    public static final String MESSAGE_TIME_CONSTRAINTS =
            "Activity end time must be later than its start time on the same day.";

    private final int id;
    private final ActivityName name;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Recurrence recurrence;

    /**
     * Creates a schedule with an ID assigned by the caller.
     * Past dates are permitted; booking policies belong to the model.
     *
     * @throws NullPointerException if any detail is null.
     * @throws IllegalArgumentException if the ID is negative or the time range is invalid.
     */
    public Activity(int id, ActivityName name, LocalDate startDate, LocalTime startTime,
            LocalTime endTime, Recurrence recurrence) {
        requireAllNonNull(name, startDate, startTime, endTime, recurrence);
        checkArgument(id >= 0, "Activity ID must be nonnegative.");
        checkArgument(endTime.isAfter(startTime), MESSAGE_TIME_CONSTRAINTS);
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrence = recurrence;
    }

    public int getId() {
        return id;
    }

    public ActivityName getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    /**
     * Returns whether this schedule occurs on the supplied date, including its first date.
     */
    public boolean occursOn(LocalDate date) {
        requireNonNull(date);
        if (date.isBefore(startDate)) {
            return false;
        }
        return switch (recurrence) {
            case ONCE -> date.equals(startDate);
            case DAILY -> true;
            case WEEKLY -> date.getDayOfWeek() == startDate.getDayOfWeek();
            default -> throw new AssertionError("Unknown recurrence: " + recurrence);
        };
    }

    /**
     * Returns the first occurrence on or after the supplied date without generating future lists.
     * Empty means there is no occurrence within Java's representable date range.
     */
    public Optional<LocalDate> nextOccurrenceOnOrAfter(LocalDate date) {
        requireNonNull(date);
        if (!date.isAfter(startDate)) {
            return Optional.of(startDate);
        }
        switch (recurrence) {
            case ONCE:
                return Optional.empty();
            case DAILY:
                return Optional.of(date);
            case WEEKLY:
                int daysUntil = Math.floorMod(startDate.getDayOfWeek().getValue()
                        - date.getDayOfWeek().getValue(), 7);
                if (LocalDate.MAX.toEpochDay() - date.toEpochDay() < daysUntil) {
                    return Optional.empty();
                }
                return Optional.of(date.plusDays(daysUntil));
            default:
                throw new AssertionError("Unknown recurrence: " + recurrence);
        }
    }

    /**
     * Returns a renamed schedule with the same identity and timing.
     */
    public Activity withName(ActivityName newName) {
        return new Activity(id, newName, startDate, startTime, endTime, recurrence);
    }

    /**
     * Returns a replacement schedule retaining this ID and name.
     * The model must validate existing booking dates and conflicts before storing the replacement.
     */
    public Activity withSchedule(LocalDate newStartDate, LocalTime newStartTime,
            LocalTime newEndTime, Recurrence newRecurrence) {
        return new Activity(id, name, newStartDate, newStartTime, newEndTime, newRecurrence);
    }

    /**
     * Returns whether the other schedule has the same persistent identity.
     */
    public boolean isSameActivity(Activity other) {
        return other != null && id == other.id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Activity activity)) {
            return false;
        }
        return id == activity.id && name.equals(activity.name) && startDate.equals(activity.startDate)
                && startTime.equals(activity.startTime) && endTime.equals(activity.endTime)
                && recurrence == activity.recurrence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDate, startTime, endTime, recurrence);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("id", id).add("name", name).add("startDate", startDate)
                .add("startTime", startTime).add("endTime", endTime).add("recurrence", recurrence).toString();
    }
}
