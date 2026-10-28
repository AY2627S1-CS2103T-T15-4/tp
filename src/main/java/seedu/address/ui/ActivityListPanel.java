package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;

/** Displays activity cards in a list. */
public class ActivityListPanel extends UiPart<Region> {
    private static final String FXML = "RecordListPanel.fxml";

    @FXML
    private ListView<Supplier<Region>> recordListView;

    private final ObservableList<Activity> activities;
    private final Function<Activity, String> enrolmentSummary;

    /** Creates a panel that displays the supplied activities. */
    public ActivityListPanel(ObservableList<Activity> activities) {
        this(activities, null, activity -> "");
    }

    /** Creates a panel with a dynamic activity-specific enrolment summary. */
    public ActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments,
            Function<Activity, String> enrolmentSummary) {
        this(activities, enrolments, null, enrolmentSummary);
    }

    /** Creates a panel that also refreshes when the search enrolment context changes. */
    public ActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments,
            Observable contextChanges, Function<Activity, String> enrolmentSummary) {
        super(FXML);
        this.activities = activities;
        this.enrolmentSummary = enrolmentSummary;
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        activities.addListener((ListChangeListener<Activity>) change -> updateActivities());
        if (enrolments != null) {
            enrolments.addListener((ListChangeListener<Enrolment>) change -> updateActivities());
        }
        if (contextChanges != null) {
            contextChanges.addListener(observable -> updateActivities());
        }
        updateActivities();
    }

    private void updateActivities() {
        List<Supplier<Region>> cards = new ArrayList<>();
        activities.forEach(activity -> cards.add(() -> new ActivityCard(activity,
                enrolmentSummary.apply(activity)).getRoot()));
        recordListView.getItems().setAll(cards);
    }

    private static class RecordListViewCell extends ListCell<Supplier<Region>> {
        @Override
        protected void updateItem(Supplier<Region> card, boolean empty) {
            super.updateItem(card, empty);
            setText(null);
            setGraphic(empty || card == null ? null : card.get());
        }
    }
}
