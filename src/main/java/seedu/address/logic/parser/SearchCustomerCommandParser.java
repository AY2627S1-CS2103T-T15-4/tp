package seedu.address.logic.parser;

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
        return SearchFieldParser.parse(args, SEARCH_FORMAT, SearchCustomerCommand.MESSAGE_USAGE,
                name -> new SearchCustomerCommand(new CustomerSearchPredicate(name)),
                id -> new SearchCustomerCommand(new CustomerSearchPredicate(id)));
    }
}
