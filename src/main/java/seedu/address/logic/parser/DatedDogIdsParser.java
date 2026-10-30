package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses fields shared by the enrol and unenrol commands, preserving each command's error guidance.
 */
final class DatedDogIdsParser {
    private static final Pattern FIELD_FORMAT = Pattern.compile(
            "(?<prefix>a|date|d)/(?<value>.*?)(?=\\s+(?:a|date|d)/|$)");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String MESSAGE_INVALID_DATE =
            "Date must be a real calendar date in YYYY-MM-DD format.";
    private static final String MESSAGE_INVALID_ACTIVITY_ID =
            "Please provide a positive, valid activity ID.";
    private static final String MESSAGE_INVALID_DOG_IDS =
            "Please provide positive, valid dog IDs separated by spaces.";

    private DatedDogIdsParser() {
    }

    /**
     * Returns validated fields from either command, with its guidance appended to any parse error.
     *
     * @param args The command arguments.
     * @param formatGuidance Command-specific format guidance for errors.
     * @return The activity ID, occurrence date, and distinct dog IDs.
     * @throws ParseException If any field is missing, malformed, or repeated.
     */
    static ParsedDogIds parse(String args, String formatGuidance) throws ParseException {
        requireNonNull(args);
        requireNonNull(formatGuidance);
        String input = args.strip();
        RawFields fields = parseFields(input, formatGuidance);

        int activityId;
        try {
            activityId = Integer.parseInt(fields.activityId());
        } catch (NumberFormatException e) {
            throw error(MESSAGE_INVALID_ACTIVITY_ID, formatGuidance, e);
        }
        if (activityId < 1) {
            throw error(MESSAGE_INVALID_ACTIVITY_ID, formatGuidance);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(fields.date(), DATE_FORMAT);
        } catch (DateTimeException e) {
            throw error(MESSAGE_INVALID_DATE, formatGuidance, e);
        }

        List<Integer> dogIds = new ArrayList<>();
        try {
            for (String dogId : fields.dogIds().split("\\s+")) {
                dogIds.add(Integer.parseInt(dogId));
            }
        } catch (NumberFormatException e) {
            throw error(MESSAGE_INVALID_DOG_IDS, formatGuidance, e);
        }
        if (dogIds.stream().anyMatch(dogId -> dogId < 1)) {
            throw error(MESSAGE_INVALID_DOG_IDS, formatGuidance);
        }
        Set<Integer> seenDogIds = new HashSet<>();
        for (int dogId : dogIds) {
            if (!seenDogIds.add(dogId)) {
                throw error("Dog ID " + dogId
                        + " is listed more than once. Please provide each dog ID only once.", formatGuidance);
            }
        }
        return new ParsedDogIds(activityId, date, List.copyOf(dogIds));
    }

    /** Parses the three prefixed fields while rejecting stray text and repeated prefixes. */
    private static RawFields parseFields(String input, String formatGuidance) throws ParseException {
        String activityId = null;
        String date = null;
        String dogIds = null;
        Set<String> prefixes = new HashSet<>();
        int end = 0;
        Matcher matcher = FIELD_FORMAT.matcher(input);
        while (matcher.find()) {
            if (!input.substring(end, matcher.start()).isBlank()) {
                throw error("Invalid command format!", formatGuidance);
            }
            String prefix = matcher.group("prefix");
            if (!prefixes.add(prefix)) {
                throw error("Multiple values specified for the following single-valued field(s): " + prefix + "/",
                        formatGuidance);
            }
            String value = matcher.group("value").trim();
            switch (prefix) {
                case "a" -> activityId = value;
                case "date" -> date = value;
                case "d" -> dogIds = value;
                default -> throw error("Invalid command format!", formatGuidance);
            }
            end = matcher.end();
        }
        if ((end == 0 && !input.isBlank()) || !input.substring(end).isBlank()) {
            throw error("Invalid command format!", formatGuidance);
        }
        if (activityId == null || activityId.isBlank()) {
            throw error("Please enter an activity ID.", formatGuidance);
        }
        if (date == null || date.isBlank()) {
            throw error("Please enter a date.", formatGuidance);
        }
        if (dogIds == null || dogIds.isBlank()) {
            throw error("Please enter at least one dog ID.", formatGuidance);
        }
        return new RawFields(activityId, date, dogIds);
    }

    /** Returns a command-specific parse error. */
    private static ParseException error(String message, String formatGuidance) {
        return new ParseException(message + formatGuidance);
    }

    /** Returns a command-specific parse error retaining its cause. */
    private static ParseException error(String message, String formatGuidance, Exception cause) {
        return new ParseException(message + formatGuidance, cause);
    }

    /** Holds validated fields shared by the two commands. */
    record ParsedDogIds(int activityId, LocalDate date, List<Integer> dogIds) {
    }

    /** Holds the three prefixed fields before their values are validated. */
    private record RawFields(String activityId, String date, String dogIds) {
    }
}
