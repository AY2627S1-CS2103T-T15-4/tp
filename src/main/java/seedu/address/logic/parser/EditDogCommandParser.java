package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SIZE;

import seedu.address.logic.commands.EditDogCommand;
import seedu.address.logic.commands.EditDogCommand.EditDogDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses input arguments and creates an {@link EditDogCommand}. */
public class EditDogCommandParser implements Parser<EditDogCommand> {

    /**
     * Parses the arguments for an {@code EditDogCommand}.
     *
     * @throws ParseException if the input does not follow the expected format
     */
    @Override
    public EditDogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);

        int dogId = parseDogId(argMultimap.getPreamble());
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);

        EditDogDescriptor descriptor = new EditDogDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(ParserUtil.parseDogName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_BREED).isPresent()) {
            descriptor.setBreed(ParserUtil.parseBreed(argMultimap.getValue(PREFIX_BREED).get()));
        }
        if (argMultimap.getValue(PREFIX_SIZE).isPresent()) {
            descriptor.setSize(ParserUtil.parseSize(argMultimap.getValue(PREFIX_SIZE).get()));
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditDogCommand.MESSAGE_NOT_EDITED);
        }
        return new EditDogCommand(dogId, descriptor);
    }

    private static int parseDogId(String value) throws ParseException {
        try {
            int dogId = Integer.parseInt(value.trim());
            if (dogId < 0) {
                throw new NumberFormatException();
            }
            return dogId;
        } catch (NumberFormatException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    EditDogCommand.MESSAGE_USAGE), exception);
        }
    }
}
