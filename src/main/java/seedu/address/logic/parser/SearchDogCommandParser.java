package seedu.address.logic.parser;

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
        return SearchFieldParser.parse(args, SEARCH_FORMAT, SearchDogCommand.MESSAGE_USAGE,
                name -> new SearchDogCommand(new DogSearchPredicate(name)),
                id -> new SearchDogCommand(new DogSearchPredicate(id)));
    }
}
