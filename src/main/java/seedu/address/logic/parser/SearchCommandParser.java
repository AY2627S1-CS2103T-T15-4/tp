package seedu.address.logic.parser;

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
            return new SearchDogCommandParser().parse(args.strip().substring(category.length()).strip());
        }
        if (category.equals("customer")) {
            return new SearchCustomerCommandParser().parse(args.strip().substring(category.length()).strip());
        }
        return SearchFieldParser.parse(args, SEARCH_FORMAT, SearchCommand.MESSAGE_USAGE,
                SearchCommand::new, SearchCommand::new);
    }
}
