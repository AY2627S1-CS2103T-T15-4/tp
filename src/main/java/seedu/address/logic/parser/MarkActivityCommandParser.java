package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.MarkActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the activity ID for a mark command. */
public class MarkActivityCommandParser implements Parser<MarkActivityCommand> {
    @Override
    public MarkActivityCommand parse(String args) throws ParseException {
        try {
            String[] fields = args.trim().split("\\s+");
            if (fields.length != 1) {
                throw new NumberFormatException();
            }
            return new MarkActivityCommand(Integer.parseInt(fields[0]));
        } catch (IllegalArgumentException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MarkActivityCommand.MESSAGE_USAGE), e);
        }
    }
}
