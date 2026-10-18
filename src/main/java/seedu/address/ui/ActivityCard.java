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

    /** Creates a card with the given activity to display. */
    public ActivityCard(Activity activity) {
        super(FXML);
        id.setText(activity.getId() + ". ");
        name.setText(activity.getName().fullName);
        date.setText("Date: " + activity.getStartDate());
        time.setText("Time: " + activity.getStartTime() + " - " + activity.getEndTime());
        recurrence.setText("Recurrence: " + activity.getRecurrence());
    }
}
