package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Routes category searches and parses a global name or ID search. */
public class SearchCommandParser implements Parser<Command> {
    // A slash cannot occur in a customer or dog name. Excluding it also rejects extra prefixes.
    private static final Pattern SEARCH_FORMAT = Pattern.compile("(?<field>n|id)/(?<value>[^/]+)");

    @Override
    public Command parse(String args) throws ParseException {
        String category = args.strip().split("\\s+", 2)[0];
        if (category.equals("dog")) {
            return new SearchDogCommandParser().parse(args);
        }
        if (category.equals("customer")) {
            return new SearchCustomerCommandParser().parse(args);
        }
        Matcher matcher = SEARCH_FORMAT.matcher(args.strip());
        if (!matcher.matches() || matcher.group("value").isBlank()) {
            throw invalidFormat();
        }
        String value = matcher.group("value").strip();
        if (matcher.group("field").equals("n")) {
            return new SearchCommand(value);
        }
        if (!value.matches("[0-9]+")) {
            throw invalidFormat();
        }
        try {
            return new SearchCommand(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCommand.MESSAGE_USAGE));
    }
}
