package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.activity.Activity;

/** Displays an activity's persistent ID, schedule details, and supplied dated-booking summary. */
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
    private Label dogSummary;

    /**
     * Creates a card with the given activity and complete dog-summary label.
     *
     * @param activity schedule to display
     * @param dogSummaryText complete dated-booking summary
     */
    public ActivityCard(Activity activity, String dogSummaryText) {
        super(FXML);
        id.setText(activity.getId() + ". ");
        name.setText(activity.getName().fullName);
        date.setText("Schedule starts: " + activity.getStartDate());
        time.setText("Time: " + activity.getStartTime() + " - " + activity.getEndTime());
        recurrence.setText("Recurrence: " + activity.getRecurrence());
        dogSummary.setText(dogSummaryText);
    }
}
