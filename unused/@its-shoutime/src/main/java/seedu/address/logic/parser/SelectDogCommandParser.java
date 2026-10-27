package seedu.address.logic.parser;
//@@author its-shoutime-unused
// Archived because select dog is not part of the final product.

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.SelectDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code select dog}. */
public class SelectDogCommandParser implements Parser<SelectDogCommand> {

    public static final String MESSAGE_INVALID_FORMAT = "Invalid format. "
            + "Please use the following format: select dog <id>\n"
            + "Example: select dog 21";

    @Override
    public SelectDogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] tokens = args.trim().split("\\s+", 2);
        if (!tokens[0].equals("dog")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        String idText = tokens.length == 2 ? tokens[1].trim() : "";
        if (!idText.matches("\\d+")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        try {
            int dogId = Integer.parseInt(idText);
            if (dogId < 1) {
                throw new ParseException(MESSAGE_INVALID_FORMAT);
            }
            return new SelectDogCommand(dogId);
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_FORMAT, exception);
        }
    }
}
