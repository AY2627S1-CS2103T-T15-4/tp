package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FilterCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for the {@code filterc} command. */
public class FilterCustomerCommandParser implements Parser<FilterCustomerCommand> {
    private static final Pattern FIELD_FORMAT = Pattern.compile(
            "(?<prefix>n|p|e|d|bc)/(?<value>.*?)(?=\\s+[A-Za-z]+/|$)");
    private static final String MESSAGE_INVALID_INFO = "Error in filtering customers:\n"
            + "Info is in the wrong format. Input in the correct format";

    @Override
    public FilterCustomerCommand parse(String args) throws ParseException {
        String input = args.trim();
        if (input.isEmpty()) {
            throw invalidFormat();
        }
        String name = null;
        String phone = null;
        String email = null;
        String backupContact = null;
        Set<Integer> dogIds = null;
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
                    name = value.replaceAll("\\s+", " ");
                }
                case "p" -> phone = parsePhone(value, phone);
                case "e" -> email = parseSingle(value, email);
                case "bc" -> backupContact = parsePhone(value, backupContact);
                case "d" -> {
                    if (dogIds != null) {
                        throw invalidFormat();
                    }
                    dogIds = parseDogIds(value);
                }
                default -> throw invalidFormat();
            }
            end = matcher.end();
        }
        if (end == 0 || !input.substring(end).isBlank()) {
            throw invalidFormat();
        }
        return new FilterCustomerCommand(name, phone, email, backupContact, dogIds);
    }

    private static String parsePhone(String value, String existing) throws ParseException {
        if (existing != null || !value.matches("\\d{8}")) {
            throw new ParseException(MESSAGE_INVALID_INFO);
        }
        return value;
    }

    private static String parseSingle(String value, String existing) throws ParseException {
        if (existing != null) {
            throw invalidFormat();
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }

    private static Set<Integer> parseDogIds(String value) throws ParseException {
        Set<Integer> dogIds = new LinkedHashSet<>();
        for (String token : value.split("\\s+")) {
            try {
                int id = Integer.parseInt(token);
                if (id < 0) {
                    throw new NumberFormatException();
                }
                dogIds.add(id);
            } catch (NumberFormatException exception) {
                throw new ParseException(MESSAGE_INVALID_INFO, exception);
            }
        }
        return dogIds;
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FilterCustomerCommand.MESSAGE_USAGE));
    }
}
