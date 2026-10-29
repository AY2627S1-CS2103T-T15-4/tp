package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.activity.Enrolment;

/** Tests the date-sensitive activity-card booking summaries without JavaFX. */
public class ActivityBookingSummaryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);

    @Test
    public void upcomingBookings_emptyEnrolments_returnsNone() {
        assertEquals("Dogs with upcoming bookings: None",
                ActivityBookingSummary.upcomingBookings(List.of(), 3, TODAY));
    }

    @Test
    public void upcomingBookings_filtersDatesAndActivitiesThenDeduplicatesAndSorts() {
        List<Enrolment> enrolments = List.of(
                new Enrolment(3, 7, TODAY.minusDays(1)),
                new Enrolment(3, 10, TODAY.minusDays(1)),
                new Enrolment(3, 10, TODAY),
                new Enrolment(3, 2, TODAY.plusDays(1)),
                new Enrolment(3, 2, TODAY.plusDays(2)),
                new Enrolment(4, 1, TODAY));

        assertEquals("Dogs with upcoming bookings: 2, 10",
                ActivityBookingSummary.upcomingBookings(enrolments, 3, TODAY));
    }

    @Test
    public void bookingsOn_includesOnlySelectedDateAndActivity() {
        List<Enrolment> enrolments = List.of(
                new Enrolment(3, 7, TODAY.minusDays(1)),
                new Enrolment(3, 10, TODAY.minusDays(1)),
                new Enrolment(3, 10, TODAY),
                new Enrolment(3, 2, TODAY),
                new Enrolment(3, 2, TODAY.plusDays(1)),
                new Enrolment(3, 8, TODAY.plusDays(1)),
                new Enrolment(4, 1, TODAY));

        assertEquals("Dogs enrolled on 2026-10-28: 2, 10",
                ActivityBookingSummary.bookingsOn(enrolments, 3, TODAY));
        assertEquals("Dogs enrolled on 2026-10-28: None",
                ActivityBookingSummary.bookingsOn(enrolments, 5, TODAY));
    }
}
