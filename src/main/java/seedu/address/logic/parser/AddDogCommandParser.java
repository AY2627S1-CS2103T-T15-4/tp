package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SIZE;

import java.util.stream.Stream;

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
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddDogCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddDogCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_BREED, PREFIX_SIZE);
        DogName name = ParserUtil.parseDogName(argMultimap.getValue(PREFIX_NAME).get());
        Breed breed = ParserUtil.parseBreed(argMultimap.getValue(PREFIX_BREED).get());
        Size size = ParserUtil.parseSize(argMultimap.getValue(PREFIX_SIZE).get());

        Dog dog = new Dog(name, breed, size);

        return new AddDogCommand(dog);
    }

    /**
     * Returns true if all specified prefixes have values in the given {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}