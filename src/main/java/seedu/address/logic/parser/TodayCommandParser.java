package seedu.address.logic.parser;

import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the {@code today} command, which accepts no arguments. */
public class TodayCommandParser implements Parser<TodayCommand> {
    private static final String MESSAGE_INVALID_INFO = "Error in showing today:\n"
            + "Info is in the wrong format. Input in the correct format";

    @Override
    public TodayCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(MESSAGE_INVALID_INFO);
        }
        return new TodayCommand();
    }
}
