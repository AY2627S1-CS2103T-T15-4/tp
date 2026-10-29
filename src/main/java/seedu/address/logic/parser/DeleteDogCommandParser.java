package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.DeleteDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for the {@code delete dog} command. */
public class DeleteDogCommandParser implements Parser<DeleteDogCommand> {

    private static final String MESSAGE_INVALID_FORMAT = "Error in deleting dog:\nInvalid format. "
            + "Please use the following format: delete <category> <id>\nExample: delete dog 12";

    @Override
    public DeleteDogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        try {
            int dogId = Integer.parseInt(trimmedArgs);
            if (dogId < 1) {
                throw new NumberFormatException();
            }
            return new DeleteDogCommand(dogId);
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_FORMAT, exception);
        }
    }
}
