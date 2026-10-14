package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.CustomerSearchPredicate;

/** Parses exactly one name or ID search field for customers. */
public class SearchCustomerCommandParser implements Parser<SearchCustomerCommand> {
    // A slash cannot occur in a customer name. Excluding it also rejects extra or unknown prefixes.
    private static final Pattern SEARCH_FORMAT = Pattern.compile("customer\\s+(?<field>n|id)/(?<value>[^/]+)");

    @Override
    public SearchCustomerCommand parse(String args) throws ParseException {
        Matcher matcher = SEARCH_FORMAT.matcher(args.strip());
        if (!matcher.matches()) {
            throw invalidFormat();
        }
        String value = matcher.group("value").strip();
        if (matcher.group("field").equals("n")) {
            return new SearchCustomerCommand(new CustomerSearchPredicate(value));
        }
        if (!value.matches("[0-9]+")) {
            throw invalidFormat();
        }
        try {
            return new SearchCustomerCommand(new CustomerSearchPredicate(Integer.parseInt(value)));
        } catch (NumberFormatException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCustomerCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCustomerCommand.MESSAGE_USAGE));
    }
}
