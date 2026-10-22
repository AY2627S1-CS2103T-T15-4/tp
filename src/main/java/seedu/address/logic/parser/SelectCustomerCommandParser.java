package seedu.address.logic.parser;

import seedu.address.logic.commands.SelectCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code select customer}. */
public class SelectCustomerCommandParser implements Parser<SelectCustomerCommand> {

    public static final String MESSAGE_INVALID_FORMAT = "Invalid format. "
            + "Please use the following format: select customer <id>\n"
            + "Example: select customer 21";

    @Override
    public SelectCustomerCommand parse(String args) throws ParseException {
        String[] tokens = args.trim().split("\\s+", 2);
        if (tokens.length == 0 || !tokens[0].equals("customer")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        String idText = tokens.length == 2 ? tokens[1].trim() : "";
        if (!idText.matches("\\d+")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        try {
            int customerId = Integer.parseInt(idText);
            if (customerId < 1) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
            return new SelectCustomerCommand(customerId);
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_FORMAT, exception);
        }
    }
}
