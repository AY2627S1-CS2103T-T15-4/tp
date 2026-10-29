package seedu.address.logic.parser;

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

import seedu.address.logic.commands.EnrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses one activity ID, one occurrence date, and a whitespace-separated list of dog IDs in any field order. */
public class EnrolDogCommandParser implements Parser<EnrolDogCommand> {
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

    @Override
    public EnrolDogCommand parse(String args) throws ParseException {
        String input = args.strip();
        ParsedFields fields = parseFields(input);

        int activityId;
        try {
            activityId = Integer.parseInt(fields.activityId());
        } catch (NumberFormatException e) {
            throw error(MESSAGE_INVALID_ACTIVITY_ID, e);
        }
        if (activityId < 1) {
            throw error(MESSAGE_INVALID_ACTIVITY_ID);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(fields.date(), DATE_FORMAT);
        } catch (DateTimeException e) {
            throw error(MESSAGE_INVALID_DATE, e);
        }

        List<Integer> dogIds = new ArrayList<>();
        try {
            for (String dogId : fields.dogIds().split("\\s+")) {
                dogIds.add(Integer.parseInt(dogId));
            }
        } catch (NumberFormatException e) {
            throw error(MESSAGE_INVALID_DOG_IDS, e);
        }
        if (dogIds.stream().anyMatch(dogId -> dogId < 1)) {
            throw error(MESSAGE_INVALID_DOG_IDS);
        }
        HashSet<Integer> seenDogIds = new HashSet<>();
        for (int dogId : dogIds) {
            if (!seenDogIds.add(dogId)) {
                throw error("Dog ID " + dogId
                        + " is listed more than once. Please provide each dog ID only once.");
            }
        }
        return new EnrolDogCommand(activityId, date, dogIds);
    }

    private ParseException invalidFormat() {
        return error("Invalid command format!");
    }

    private ParseException error(String message) {
        return new ParseException(message + EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE);
    }

    private ParseException error(String message, Exception cause) {
        return new ParseException(message + EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE, cause);
    }

    /** Parses the three prefixed fields while rejecting stray text and repeated prefixes. */
    private ParsedFields parseFields(String input) throws ParseException {
        String activityId = null;
        String date = null;
        String dogIds = null;
        Set<String> prefixes = new HashSet<>();
        int end = 0;
        Matcher matcher = FIELD_FORMAT.matcher(input);
        while (matcher.find()) {
            if (!input.substring(end, matcher.start()).isBlank()) {
                throw invalidFormat();
            }
            String prefix = matcher.group("prefix");
            if (!prefixes.add(prefix)) {
                throw error("Multiple values specified for the following single-valued field(s): " + prefix + "/");
            }
            String value = matcher.group("value").trim();
            switch (prefix) {
                case "a" -> activityId = value;
                case "date" -> date = value;
                case "d" -> dogIds = value;
                default -> throw invalidFormat();
            }
            end = matcher.end();
        }
        if ((end == 0 && !input.isBlank()) || !input.substring(end).isBlank()) {
            throw invalidFormat();
        }
        if (activityId == null || activityId.isBlank()) {
            throw error("Please enter an activity ID.");
        }
        if (date == null || date.isBlank()) {
            throw error("Please enter a date.");
        }
        if (dogIds == null || dogIds.isBlank()) {
            throw error("Please enter at least one dog ID.");
        }
        return new ParsedFields(activityId, date, dogIds);
    }

    private record ParsedFields(String activityId, String date, String dogIds) {}
}
