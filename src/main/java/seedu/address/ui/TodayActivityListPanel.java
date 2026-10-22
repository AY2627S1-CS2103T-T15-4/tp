package seedu.address.ui;

import java.time.LocalDate;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.model.activity.Activity;

/** Displays activities that occur on the current date. */
public class TodayActivityListPanel extends ActivityListPanel {

    /** Creates a panel backed by the application's activity list. */
    public TodayActivityListPanel(ObservableList<Activity> activities) {
        super(new FilteredList<>(activities, activity -> activity.occursOn(LocalDate.now())));
    }
}
