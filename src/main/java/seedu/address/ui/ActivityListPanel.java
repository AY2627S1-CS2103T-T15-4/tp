package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    private final ObservableList<Enrolment> enrolments;
    private final Clock clock;
    private final boolean todayPanel;

    /**
     * Creates a general panel showing upcoming dog bookings for the supplied activities.
     *
     * @param activities Schedules permitted by the current activity filter.
     * @param enrolments All stored dated enrolments.
     */
    public ActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments) {
        this(activities, enrolments, Clock.systemDefaultZone(), false);
    }

    /**
     * Creates a date-sensitive panel with an injectable clock.
     *
     * @param activities Schedules available to the panel.
     * @param enrolments All stored dated enrolments.
     * @param clock Local-date source.
     * @param todayPanel Whether to show only today's schedules and bookings.
     */
    protected ActivityListPanel(ObservableList<Activity> activities, ObservableList<Enrolment> enrolments,
            Clock clock, boolean todayPanel) {
        super(FXML);
        this.activities = requireNonNull(activities);
        this.enrolments = requireNonNull(enrolments);
        this.clock = requireNonNull(clock);
        this.todayPanel = todayPanel;
        recordListView.setPlaceholder(createEmptyState(todayPanel
                ? "No activities today."
                : "No activities to display."));
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        activities.addListener((ListChangeListener<Activity>) change -> refresh());
        enrolments.addListener((ListChangeListener<Enrolment>) change -> refresh());
        refresh();
    }

    /** Reconciles cards using one captured current date for this update. */
    public void refresh() {
        refresh(LocalDate.now(clock));
    }

    /**
     * Reconciles cards using the supplied current date.
     *
     * @param today Current local date shared across the coordinated panel refresh.
     */
    public void refresh(LocalDate today) {
        requireNonNull(today);
        List<CardListEntry> cards = new ArrayList<>();
        activities.stream()
                .filter(activity -> !todayPanel || activity.occursOn(today))
                .forEach(activity -> {
                    String summary = todayPanel
                            ? ActivityBookingSummary.bookingsOn(enrolments, activity.getId(), today)
                            : ActivityBookingSummary.upcomingBookings(enrolments, activity.getId(), today);
                    Supplier<Region> cardFactory = () -> new ActivityCard(activity, summary).getRoot();
                    Object contentSnapshot = List.of(activity, summary);
                    cards.add(new CardListEntry("activity:" + activity.getId(), contentSnapshot, cardFactory));
                });
        CardListUpdater.reconcile(recordListView, cards);
    }

    /** Scrolls to the first activity after an explicit display-changing command. */
    void scrollToTop() {
        CardListUpdater.scrollToTop(recordListView);
    }

    private static Label createEmptyState(String message) {
        Label placeholder = new Label(message);
        placeholder.getStyleClass().add("empty-state");
        return placeholder;
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
