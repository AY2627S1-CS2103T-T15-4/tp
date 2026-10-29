// @@author lilosy
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.DeleteActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the persistent ID for the {@code deleteactivity} command.
 */
public class DeleteActivityCommandParser implements Parser<DeleteActivityCommand> {
    @Override
    public DeleteActivityCommand parse(String args) throws ParseException {
        try {
            int activityId = Integer.parseInt(args.trim());
            if (activityId <= 0) {
                throw new NumberFormatException();
            }
            return new DeleteActivityCommand(activityId);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    DeleteActivityCommand.MESSAGE_USAGE), e);
        }
    }
}
