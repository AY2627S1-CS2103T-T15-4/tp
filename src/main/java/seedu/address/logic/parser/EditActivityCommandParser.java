// @@author lilosy
package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RECURRENCE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.EditActivityCommand;
import seedu.address.logic.commands.EditActivityCommand.EditActivityDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.Recurrence;

/**
 * Parses optional edits to an existing activity series.
 */
public class EditActivityCommandParser implements Parser<EditActivityCommand> {
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?:^|\\s)([A-Za-z]+/)");
    private static final Set<String> ALLOWED_PREFIXES = Set.of("n/", "date/", "start/", "end/", "r/");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Parses an activity ID followed by one or more replacement fields. */
    @Override
    public EditActivityCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = PREFIX_TOKEN.matcher(args);
        while (matcher.find()) {
            if (!ALLOWED_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException("Unknown activity prefix: " + matcher.group(1));
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_DATE,
                PREFIX_START, PREFIX_END, PREFIX_RECURRENCE);
        int activityId = parseActivityId(arguments.getPreamble());
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_DATE, PREFIX_START, PREFIX_END,
                PREFIX_RECURRENCE);

        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        if (arguments.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(parseField("activity name (n/)", ParserUtil::parseActivityName,
                    arguments.getValue(PREFIX_NAME).get()));
        }
        if (arguments.getValue(PREFIX_DATE).isPresent()) {
            descriptor.setDate(parseField("activity date (date/)", EditActivityCommandParser::parseDate,
                    arguments.getValue(PREFIX_DATE).get()));
        }
        if (arguments.getValue(PREFIX_START).isPresent()) {
            descriptor.setStartTime(parseField("activity start time (start/)", EditActivityCommandParser::parseTime,
                    arguments.getValue(PREFIX_START).get()));
        }
        if (arguments.getValue(PREFIX_END).isPresent()) {
            descriptor.setEndTime(parseField("activity end time (end/)", EditActivityCommandParser::parseTime,
                    arguments.getValue(PREFIX_END).get()));
        }
        if (arguments.getValue(PREFIX_RECURRENCE).isPresent()) {
            descriptor.setRecurrence(parseField("activity recurrence (r/)", EditActivityCommandParser::parseRecurrence,
                    arguments.getValue(PREFIX_RECURRENCE).get()));
        }
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditActivityCommand.MESSAGE_NOT_EDITED);
        }
        return new EditActivityCommand(activityId, descriptor);
    }

    /** Identifies the field while preserving its existing validation guidance. */
    private static <T> T parseField(String field, FieldParser<T> parser, String value) throws ParseException {
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            throw new ParseException("Invalid " + field + ": " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }

    private static int parseActivityId(String value) throws ParseException {
        String trimmedId = value.trim();
        if (trimmedId.isEmpty()) {
            throw new ParseException("Activity ID is required. Enter a positive whole number.");
        }
        try {
            int activityId = Integer.parseInt(trimmedId);
            if (activityId <= 0) {
                throw new NumberFormatException();
            }
            return activityId;
        } catch (NumberFormatException e) {
            throw new ParseException("Please provide a positive, valid activity ID.", e);
        }
    }

    private static LocalDate parseDate(String value) throws ParseException {
        try {
            if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                throw new ParseException(AddActivityCommandParser.MESSAGE_DATE);
            }
            return LocalDate.parse(value);
        } catch (DateTimeException e) {
            throw new ParseException(AddActivityCommandParser.MESSAGE_DATE, e);
        }
    }

    private static LocalTime parseTime(String value) throws ParseException {
        try {
            if (!value.matches("[0-9]{2}:[0-9]{2}")) {
                throw new ParseException(AddActivityCommandParser.MESSAGE_TIME);
            }
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (DateTimeException e) {
            throw new ParseException(AddActivityCommandParser.MESSAGE_TIME, e);
        }
    }

    private static Recurrence parseRecurrence(String value) throws ParseException {
        return switch (value) {
            case "once" -> Recurrence.ONCE;
            case "daily" -> Recurrence.DAILY;
            case "weekly" -> Recurrence.WEEKLY;
            default -> throw new ParseException(AddActivityCommandParser.MESSAGE_RECURRENCE);
        };
    }
}
