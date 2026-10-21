package seedu.address.logic.parser;

import seedu.address.logic.commands.SelectDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code select dog}. */
public class SelectDogCommandParser implements Parser<SelectDogCommand> {

    public static final String MESSAGE_INVALID_FORMAT = "Invalid format. "
            + "Please use the following format: select dog <id>\n"
            + "Example: select dog 21";

    @Override
    public SelectDogCommand parse(String args) throws ParseException {
        String[] tokens = args.trim().split("\\s+", 2);
        if (tokens.length == 0 || !tokens[0].equals("dog")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        String idText = tokens.length == 2 ? tokens[1].trim() : "";
        if (!idText.matches("\\d+")) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        try {
            return new SelectDogCommand(Integer.parseInt(idText));
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_FORMAT, exception);
        }
    }
}
