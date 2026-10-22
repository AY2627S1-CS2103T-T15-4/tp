package seedu.address.logic.parser;

import seedu.address.logic.commands.FilterUnlinkedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for the {@code filterun} command. */
public class FilterUnlinkedCommandParser implements Parser<FilterUnlinkedCommand> {
    private static final String MESSAGE_INVALID_FORMAT = "Info is in the wrong format. "
            + "Please input: filterun dog/filterun customer only";

    @Override
    public FilterUnlinkedCommand parse(String args) throws ParseException {
        String category = args.trim();
        if (category.equals("dog")) {
            return new FilterUnlinkedCommand(true);
        }
        if (category.equals("customer")) {
            return new FilterUnlinkedCommand(false);
        }
        throw new ParseException(MESSAGE_INVALID_FORMAT);
    }
}
