package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
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
     * @throws ParseException If the input does not follow the expected format.
     */
    @Override
    public EditDogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);

        int dogId = parseDogId(argMultimap.getPreamble());
        try {
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }

        EditDogDescriptor descriptor = new EditDogDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            try {
                descriptor.setName(ParserUtil.parseDogName(argMultimap.getValue(PREFIX_NAME).get()));
            } catch (ParseException e) {
                throw new ParseException(e.getMessage() + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
            }
        }
        if (argMultimap.getValue(PREFIX_BREED).isPresent()) {
            try {
                descriptor.setBreed(ParserUtil.parseBreed(argMultimap.getValue(PREFIX_BREED).get()));
            } catch (ParseException e) {
                throw new ParseException(e.getMessage() + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
            }
        }
        if (argMultimap.getValue(PREFIX_SIZE).isPresent()) {
            try {
                descriptor.setSize(ParserUtil.parseSize(argMultimap.getValue(PREFIX_SIZE).get()));
            } catch (ParseException e) {
                throw new ParseException(e.getMessage() + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
            }
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditDogCommand.MESSAGE_NOT_EDITED + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }
        return new EditDogCommand(dogId, descriptor);
    }

    /** Returns a positive dog ID parsed from {@code value}. */
    private static int parseDogId(String value) throws ParseException {
        try {
            int dogId = Integer.parseInt(value.trim());
            if (dogId < 1) {
                throw new NumberFormatException();
            }
            return dogId;
        } catch (NumberFormatException exception) {
            throw new ParseException("Please provide a positive, valid dog ID."
                    + EditDogCommand.MESSAGE_FORMAT_GUIDANCE, exception);
        }
    }
}
