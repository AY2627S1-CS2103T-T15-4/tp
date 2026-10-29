package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.model.activity.Enrolment;

/** Formats activity-card dog summaries from dated enrolment records. */
public final class ActivityBookingSummary {
    private ActivityBookingSummary() {
    }

    /**
     * Returns the summary of distinct dogs booked for this activity today or later.
     *
     * @param enrolments All stored dated enrolments.
     * @param activityId Activity whose bookings are summarized.
     * @param today Inclusive lower date boundary.
     * @return The complete general-card dog summary.
     */
    public static String upcomingBookings(List<Enrolment> enrolments, int activityId, LocalDate today) {
        requireNonNull(enrolments);
        requireNonNull(today);
        List<Integer> dogIds = enrolments.stream()
                .filter(enrolment -> enrolment.getActivityId() == activityId)
                .filter(enrolment -> !enrolment.getDate().isBefore(today))
                .map(Enrolment::getDogId)
                .distinct()
                .sorted()
                .toList();
        return "Dogs with upcoming bookings: " + formatDogIds(dogIds);
    }

    /**
     * Returns the summary of distinct dogs booked for this activity on the supplied date.
     *
     * @param enrolments All stored dated enrolments.
     * @param activityId Activity whose bookings are summarized.
     * @param date Exact booking date.
     * @return The complete exact-date dog summary.
     */
    public static String bookingsOn(List<Enrolment> enrolments, int activityId, LocalDate date) {
        requireNonNull(enrolments);
        requireNonNull(date);
        List<Integer> dogIds = enrolments.stream()
                .filter(enrolment -> enrolment.getActivityId() == activityId)
                .filter(enrolment -> enrolment.getDate().equals(date))
                .map(Enrolment::getDogId)
                .distinct()
                .sorted()
                .toList();
        return "Dogs enrolled on " + date + ": " + formatDogIds(dogIds);
    }

    private static String formatDogIds(List<Integer> dogIds) {
        return dogIds.isEmpty()
                ? "None"
                : String.join(", ", dogIds.stream().map(String::valueOf).toList());
    }
}
