package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.activity.Activity;

/** A UI component that displays an activity's persistent ID and schedule details. */
public class ActivityCard extends UiPart<Region> {
    private static final String FXML = "ActivityListCard.fxml";

    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label date;
    @FXML
    private Label time;
    @FXML
    private Label recurrence;
    @FXML
    private Label enrolmentsHeading;
    @FXML
    private Label enrolments;

    /** Creates a card with the given activity to display. */
    public ActivityCard(Activity activity) {
        this(activity, "");
    }

    /** Creates a card with optional search-specific enrolment details. */
    public ActivityCard(Activity activity, String enrolmentEntry) {
        super(FXML);
        id.setText(activity.getId() + ". ");
        name.setText(activity.getName().fullName);
        date.setText((enrolmentEntry.isBlank() ? "Date: " : "Schedule starts: ") + activity.getStartDate());
        time.setText("Time: " + activity.getStartTime() + " - " + activity.getEndTime());
        recurrence.setText("Recurrence: " + activity.getRecurrence());
        enrolmentsHeading.setVisible(!enrolmentEntry.isBlank());
        enrolmentsHeading.setManaged(!enrolmentEntry.isBlank());
        enrolments.setText(enrolmentEntry.substring(enrolmentEntry.indexOf('\n') + 1));
        enrolments.setVisible(!enrolmentEntry.isBlank());
        enrolments.setManaged(!enrolmentEntry.isBlank());
    }
}
