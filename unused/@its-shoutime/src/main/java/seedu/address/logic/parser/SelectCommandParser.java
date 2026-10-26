package seedu.address.logic.parser;

//@@author its-shoutime-unused
// Archived because select is not part of the final product.

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code select dog} and {@code select customer}. */
public class SelectCommandParser implements Parser<Command> {

    public static final String MESSAGE_INVALID_FORMAT = "Invalid format. "
            + "Please use the following format: select <category> <id>\n"
            + "Example: select dog 21";

    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmed = args.trim();
        String category = trimmed.split("\\s+", 2)[0];
        if (category.equals("dog")) {
            return new SelectDogCommandParser().parse(args);
        }
        if (category.equals("customer")) {
            return new SelectCustomerCommandParser().parse(args);
        }
        throw new ParseException(MESSAGE_INVALID_FORMAT);
    }
}
