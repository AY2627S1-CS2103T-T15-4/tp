package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.activity.Activity;

/**
 * Displays an activity's persistent ID, schedule details, and supplied dated-booking summary.
 */
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
    private Label headerTime;
    @FXML
    private HBox header;
    @FXML
    private Label recurrence;
    @FXML
    private Label dogSummary;
    // @@author XanderGoh
    @FXML
    private Label enrolmentsHeading;
    @FXML
    private Label enrolments;
    // @@author

    /**
     * Creates a card with the given activity and complete dog-summary label.
     *
     * @param activity Schedule to display.
     * @param dogSummaryText Complete dated-booking summary.
     */
    public ActivityCard(Activity activity, String dogSummaryText) {
        this(activity, dogSummaryText, "");
    }

    /**
     * Creates a card with booking and search-specific enrolment summaries.
     *
     * @param activity Schedule to display.
     * @param dogSummaryText Complete dated-booking summary.
     * @param enrolmentEntry Search-specific enrolment summary.
     */
    public ActivityCard(Activity activity, String dogSummaryText, String enrolmentEntry) {
        this(activity, dogSummaryText, enrolmentEntry, false);
    }

    /**
     * Creates a card with the activity time in its header when displayed in the Today panel.
     *
     * @param activity Schedule to display.
     * @param dogSummaryText Complete dated-booking summary.
     * @param enrolmentEntry Search-specific enrolment summary.
     * @param todayCard Whether the card is displayed in the Today panel.
     */
    public ActivityCard(Activity activity, String dogSummaryText, String enrolmentEntry, boolean todayCard) {
        super(FXML);
        id.setText(activity.getId() + ". ");
        name.setText(activity.getName().fullName);
        date.setText("Schedule starts: " + activity.getStartDate());
        time.setText("Time: " + activity.getStartTime() + " - " + activity.getEndTime());
        headerTime.setText(activity.getStartTime() + "–" + activity.getEndTime());
        headerTime.setVisible(todayCard);
        headerTime.setManaged(todayCard);
        if (todayCard) {
            header.setAlignment(Pos.TOP_LEFT);
            header.setMaxWidth(Double.MAX_VALUE);
            name.setWrapText(true);
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);
        }
        time.setVisible(!todayCard);
        time.setManaged(!todayCard);
        recurrence.setText("Recurrence: " + activity.getRecurrence());
        dogSummary.setText(dogSummaryText);
        dogSummary.setVisible(!dogSummaryText.isBlank());
        dogSummary.setManaged(!dogSummaryText.isBlank());
        // @@author XanderGoh
        enrolmentsHeading.setVisible(!enrolmentEntry.isBlank());
        enrolmentsHeading.setManaged(!enrolmentEntry.isBlank());
        enrolments.setText(enrolmentEntry.substring(enrolmentEntry.indexOf('\n') + 1));
        enrolments.setVisible(!enrolmentEntry.isBlank());
        enrolments.setManaged(!enrolmentEntry.isBlank());
        // @@author
    }
}
