package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.dog.DogSearchPredicate;

/** Parses exactly one name or ID search field for dogs. */
public class SearchDogCommandParser implements Parser<SearchDogCommand> {
    // A slash cannot occur in a dog name. Excluding it also rejects extra or unknown prefixes.
    private static final Pattern SEARCH_FORMAT = Pattern.compile("dog\\s+(?<field>n|id)/(?<value>[^/]+)");

    @Override
    public SearchDogCommand parse(String args) throws ParseException {
        Matcher matcher = SEARCH_FORMAT.matcher(args.strip());
        if (!matcher.matches()) {
            throw invalidFormat();
        }
        String value = matcher.group("value").strip();
        if (matcher.group("field").equals("n")) {
            return new SearchDogCommand(new DogSearchPredicate(value));
        }
        if (!value.matches("[0-9]+")) {
            throw invalidFormat();
        }
        try {
            return new SearchDogCommand(new DogSearchPredicate(Integer.parseInt(value)));
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchDogCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchDogCommand.MESSAGE_USAGE));
    }
}
