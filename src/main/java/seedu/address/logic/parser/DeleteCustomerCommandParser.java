package seedu.address.logic.parser;

import seedu.address.logic.commands.DeleteCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for the {@code delete customer} command. */
public class DeleteCustomerCommandParser implements Parser<DeleteCustomerCommand> {

    private static final String MESSAGE_INVALID_FORMAT = "Error in deleting customer:\nInvalid format. "
            + "Please use the following format: delete <category> <id>\nExample: delete customer 21";

    @Override
    public DeleteCustomerCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        try {
            int customerId = Integer.parseInt(trimmedArgs);
            if (customerId < 0) {
                throw new NumberFormatException();
            }
            return new DeleteCustomerCommand(customerId);
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_FORMAT, exception);
        }
    }
}
