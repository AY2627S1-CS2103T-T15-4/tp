package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UnmarkActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the activity ID for an unmark command. */
public class UnmarkActivityCommandParser implements Parser<UnmarkActivityCommand> {
    @Override
    public UnmarkActivityCommand parse(String args) throws ParseException {
        try {
            String[] fields = args.trim().split("\\s+");
            if (fields.length != 1) {
                throw new NumberFormatException();
            }
            return new UnmarkActivityCommand(Integer.parseInt(fields[0]));
        } catch (IllegalArgumentException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    UnmarkActivityCommand.MESSAGE_USAGE), e);
        }
    }
}
