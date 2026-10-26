package seedu.address.model.activity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.function.Predicate;

/** Filters activities by the supplied name, date, time, and recurrence criteria. */
public class ActivitySearchPredicate implements Predicate<Activity> {
    private final String nameQuery;
    private final LocalDate date;
    private final LocalTime time;
    private final Recurrence recurrence;

    /** Creates a predicate from any combination of supported filter fields. */
    public ActivitySearchPredicate(String nameQuery, LocalDate date, LocalTime time, Recurrence recurrence) {
        this.nameQuery = nameQuery == null ? null : normalize(nameQuery);
        this.date = date;
        this.time = time;
        this.recurrence = recurrence;
    }

    @Override
    public boolean test(Activity activity) {
        return matchesName(activity) && matchesDate(activity) && matchesTime(activity)
                && matchesRecurrence(activity);
    }

    private boolean matchesName(Activity activity) {
        return nameQuery == null || normalize(activity.getName().toString()).contains(nameQuery);
    }

    private boolean matchesDate(Activity activity) {
        return date == null || activity.occursOn(date);
    }

    private boolean matchesTime(Activity activity) {
        return time == null
                || (!time.isBefore(activity.getStartTime()) && !time.isAfter(activity.getEndTime()));
    }

    private boolean matchesRecurrence(Activity activity) {
        return recurrence == null || activity.getRecurrence() == recurrence;
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
