/**
 * Activity schedule domain objects, independent of parsing, persistence, and presentation.
 *
 * <p>For example, a caller can construct a weekly schedule with
 * {@code new Activity(12, new ActivityName("Training"), LocalDate.of(2026, 10, 20),
 * LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.WEEKLY)}.
 * {@link seedu.address.model.activity.Activity#occursOn(java.time.LocalDate)} supports a dated view;
 * {@link seedu.address.model.activity.Activity#nextOccurrenceOnOrAfter(java.time.LocalDate)}
 * finds one next date without materializing an unbounded recurring series.
 *
 * <p>Identity is the persistent positive activity ID, supplied by the caller. Equality also
 * includes schedule details. Replacement methods preserve identity and leave the original unchanged.
 * Times describe a same-day interval with end later than start. Dates do not depend on today's clock.
 *
 * <p>Integration contract for subsequent contributions: the address book allocates and persists schedule IDs. Store
 * dog participation separately by (activity ID, occurrence date). An absent dated booking means
 * zero dogs; attending one date never enrols a dog in another. The model must validate references,
 * same-dog overlap, and existing bookings before accepting an edited schedule, and remove booking
 * references when a dog or activity is deleted. Booking operations are not implemented by this
 * package; schedule JSON storage is provided by the storage component. Commands and the UI should
 * use the model rather than mutate schedules.
 */
package seedu.address.model.activity;
