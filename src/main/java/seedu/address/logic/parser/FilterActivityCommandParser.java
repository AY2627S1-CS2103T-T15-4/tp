package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FilterActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.Recurrence;

/** Parses arguments for the {@code filtera} command. */
public class FilterActivityCommandParser implements Parser<FilterActivityCommand> {
    private static final Pattern FIELD_FORMAT = Pattern.compile(
            "(?<prefix>[A-Za-z]+)/(?<value>.*?)(?=\\s+[A-Za-z]+/|$)");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public FilterActivityCommand parse(String args) throws ParseException {
        String input = args.trim();
        if (input.isEmpty()) {
            throw invalidFormat();
        }

        String name = null;
        LocalDate date = null;
        LocalTime time = null;
        Recurrence recurrence = null;
        int end = 0;
        Matcher matcher = FIELD_FORMAT.matcher(input);
        while (matcher.find()) {
            if (!input.substring(end, matcher.start()).isBlank()) {
                throw invalidFormat();
            }
            String value = matcher.group("value").trim();
            if (value.isEmpty()) {
                throw invalidFormat();
            }
            switch (matcher.group("prefix")) {
                case "n" -> {
                    if (name != null) {
                        throw invalidFormat();
                    }
                    name = normalizeName(value);
                }
                case "d" -> {
                    if (date != null) {
                        throw invalidFormat();
                    }
                    date = parseDate(value);
                }
                case "t" -> {
                    if (time != null) {
                        throw invalidFormat();
                    }
                    time = parseTime(value);
                }
                case "r" -> {
                    if (recurrence != null) {
                        throw invalidFormat();
                    }
                    recurrence = parseRecurrence(value);
                }
                default -> throw invalidFormat();
            }
            end = matcher.end();
        }
        if (end == 0 || !input.substring(end).isBlank()) {
            throw invalidFormat();
        }
        return new FilterActivityCommand(name, date, time, recurrence);
    }

    private static String normalizeName(String value) {
        return value.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static LocalDate parseDate(String value) throws ParseException {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ParseException("Error in filtering activities:\n"
                    + "Activity date must be a valid date in YYYY-MM-DD format.", exception);
        }
    }

    private static LocalTime parseTime(String value) throws ParseException {
        try {
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ParseException("Error in filtering activities:\n"
                    + "Activity times must use 24-hour HH:mm format (00:00 to 23:59).", exception);
        }
    }

    private static Recurrence parseRecurrence(String value) throws ParseException {
        try {
            return Recurrence.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ParseException("Error in filtering activities:\n"
                    + "Activity recurrence must be Once, Weekly, or Daily.", exception);
        }
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FilterActivityCommand.MESSAGE_USAGE));
    }
}
