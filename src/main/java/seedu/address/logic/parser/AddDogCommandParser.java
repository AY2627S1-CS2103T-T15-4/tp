// @@author Umaiza-15
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SIZE;

import seedu.address.logic.commands.AddDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Parses input arguments and creates an {@link AddDogCommand}. */
public class AddDogCommandParser implements Parser<AddDogCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code AddDogCommand}
     * and returns an {@code AddDogCommand} for execution.
     * @throws ParseException If the user input does not conform to the expected format.
     */
    public AddDogCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddDogCommand.MESSAGE_USAGE));
        }

        try {
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }

        String nameValue = argMultimap.getValue(PREFIX_NAME).orElse("").trim();
        String breedValue = argMultimap.getValue(PREFIX_BREED).orElse("").trim();
        String sizeValue = argMultimap.getValue(PREFIX_SIZE).orElse("").trim();

        if (nameValue.isEmpty()) {
            throw missingFieldMessage("name");
        }
        if (breedValue.isEmpty()) {
            throw missingFieldMessage("breed");
        }
        if (sizeValue.isEmpty()) {
            throw missingFieldMessage("size");
        }

        DogName name;
        try {
            name = ParserUtil.parseDogName(nameValue);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }

        Breed breed;
        try {
            breed = ParserUtil.parseBreed(breedValue);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }

        Size size;
        try {
            size = ParserUtil.parseSize(sizeValue);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
        }

        Dog dog = new Dog(name, breed, size);

        return new AddDogCommand(dog);
    }

    private static ParseException missingFieldMessage(String field) {
        return new ParseException("Please enter a " + field + "." + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
    }

}
