// @@author Kenneth-Chia
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
import java.util.stream.Stream;

import seedu.address.logic.commands.AddActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Parses strictly dated, same-day activity schedules. */
public class AddActivityCommandParser implements Parser<AddActivityCommand> {
    public static final String MESSAGE_DATE = "Activity date must be a valid date in YYYY-MM-DD format.";
    public static final String MESSAGE_TIME = "Activity times must use 24-hour HH:mm format (00:00 to 23:59).";
    public static final String MESSAGE_RECURRENCE = "Recurrence must be once, daily, or weekly (lowercase).";
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?:^|\\s)([A-Za-z]+/)");
    private static final Set<String> ALLOWED_PREFIXES = Set.of("n/", "date/", "start/", "end/", "r/");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public AddActivityCommand parse(String args) throws ParseException {
        validateKnownPrefixes(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_DATE,
                PREFIX_START, PREFIX_END, PREFIX_RECURRENCE);
        validateArgumentStructure(arguments);
        ActivityName name = parseName(arguments.getValue(PREFIX_NAME).get());
        LocalDate date = parseDate(arguments.getValue(PREFIX_DATE).get());
        LocalTime start = parseTime(arguments.getValue(PREFIX_START).get());
        LocalTime end = parseTime(arguments.getValue(PREFIX_END).get());
        Recurrence recurrence = parseRecurrence(arguments.getValue(PREFIX_RECURRENCE).get());
        if (!end.isAfter(start)) {
            throw new ParseException(Activity.MESSAGE_TIME_CONSTRAINTS);
        }
        return new AddActivityCommand(name, date, start, end, recurrence);
    }

    private void validateKnownPrefixes(String args) throws ParseException {
        Matcher matcher = PREFIX_TOKEN.matcher(args);
        while (matcher.find()) {
            if (!ALLOWED_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException("Unknown activity prefix: " + matcher.group(1));
            }
        }
    }

    private void validateArgumentStructure(ArgumentMultimap arguments) throws ParseException {
        Prefix[] prefixes = {PREFIX_NAME, PREFIX_DATE, PREFIX_START, PREFIX_END, PREFIX_RECURRENCE};
        if (!arguments.getPreamble().isEmpty()
                || Stream.of(prefixes).anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddActivityCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(prefixes);
    }

    private ActivityName parseName(String value) throws ParseException {
        if (!ActivityName.isValidName(value)) {
            throw new ParseException(ActivityName.MESSAGE_CONSTRAINTS);
        }
        return new ActivityName(value);
    }

    private LocalDate parseDate(String value) throws ParseException {
        try {
            if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                throw new ParseException(MESSAGE_DATE);
            }
            return LocalDate.parse(value);
        } catch (DateTimeException e) {
            throw new ParseException(MESSAGE_DATE, e);
        }
    }

    private LocalTime parseTime(String value) throws ParseException {
        try {
            if (!value.matches("[0-9]{2}:[0-9]{2}")) {
                throw new ParseException(MESSAGE_TIME);
            }
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (DateTimeException e) {
            throw new ParseException(MESSAGE_TIME, e);
        }
    }

    private Recurrence parseRecurrence(String value) throws ParseException {
        return switch (value) {
            case "once" -> Recurrence.ONCE;
            case "daily" -> Recurrence.DAILY;
            case "weekly" -> Recurrence.WEEKLY;
            default -> throw new ParseException(MESSAGE_RECURRENCE);
        };
    }
}
