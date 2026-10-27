//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/** Shared validation for the single name or persistent ID field accepted by search commands. */
final class SearchFieldParser {
    private SearchFieldParser() {
    }

    /** Parses a search using the category's grammar, usage message and command factories. */
    static <T> T parse(String args, Pattern format, String usage,
            Function<String, T> nameSearch, IntFunction<T> idSearch) throws ParseException {
        Matcher matcher = format.matcher(args.strip());
        if (!matcher.matches()) {
            throw invalidFormat(usage);
        }
        String value = matcher.group("value").strip();
        if (matcher.group("field").equals("n")) {
            return nameSearch.apply(value);
        }
        if (!value.matches("[0-9]+")) {
            throw invalidFormat(usage);
        }
        try {
            return idSearch.apply(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage), e);
        }
    }

    private static ParseException invalidFormat(String usage) {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }
}
