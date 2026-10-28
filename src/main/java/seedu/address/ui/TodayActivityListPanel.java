package seedu.address.ui;

import java.time.Clock;

import javafx.collections.ObservableList;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;

/** Displays activities occurring on the current date with exact-date dog summaries. */
public class TodayActivityListPanel extends ActivityListPanel {

    /**
     * Creates a panel backed by all activities and dated enrolments.
     *
     * @param activities all stored activity schedules
     * @param enrolments all stored dated enrolments
     */
    public TodayActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments) {
        this(activities, enrolments, Clock.systemDefaultZone());
    }

    /** Creates a Today panel with an injectable clock for deterministic tests. */
    TodayActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments, Clock clock) {
        super(activities, enrolments, clock, true);
    }
}
