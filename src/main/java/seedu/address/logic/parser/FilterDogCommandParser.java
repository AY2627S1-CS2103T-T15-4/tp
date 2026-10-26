package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FilterDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.dog.Size;

/** Parses arguments for the {@code searchd} command. */
public class FilterDogCommandParser implements Parser<FilterDogCommand> {
    private static final Pattern FIELD_FORMAT = Pattern.compile(
            "(?<prefix>n|b|s|i)/(?<value>.*?)(?=\\s+[A-Za-z]+/|$)");
    private static final String MESSAGE_INVALID_INFO = "Error in searching dogs:\n"
            + "Info is in the wrong format. Input in the correct format";

    @Override
    public FilterDogCommand parse(String args) throws ParseException {
        String input = args.trim();
        if (input.isEmpty()) {
            throw invalidFormat();
        }
        String name = null;
        String breed = null;
        String size = null;
        Integer id = null;
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
                case "n" -> name = parseUniqueName(value, name);
                case "b" -> breed = parseUniqueName(value, breed);
                case "s" -> size = parseSize(value, size);
                case "i" -> id = parseId(value, id);
                default -> throw invalidFormat();
            }
            end = matcher.end();
        }
        if (end == 0 || !input.substring(end).isBlank()) {
            throw invalidFormat();
        }
        return new FilterDogCommand(name, breed, size, id);
    }

    private static String parseUniqueName(String value, String existing) throws ParseException {
        if (existing != null) {
            throw invalidFormat();
        }
        return value.replaceAll("\\s+", " ");
    }

    private static String parseSize(String value, String existing) throws ParseException {
        if (existing != null || !Size.isValidSize(value)) {
            throw new ParseException("Error in searching dogs:\n"
                    + "Invalid dog size. Please input small/medium/large/giant only.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private static Integer parseId(String value, Integer existing) throws ParseException {
        if (existing != null) {
            throw invalidFormat();
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_INFO, exception);
        }
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FilterDogCommand.MESSAGE_USAGE));
    }
}
