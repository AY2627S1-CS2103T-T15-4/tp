package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddDogCommand;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class AddDogCommandParserTest {

    private final AddDogCommandParser parser = new AddDogCommandParser();
    private final String validArguments = " n/Cookie b/Golden Retriever s/Large";
    private final String missingNameMessage = "Please enter a name." + AddDogCommand.MESSAGE_FORMAT_GUIDANCE;
    private final String missingBreedMessage = "Please enter a breed." + AddDogCommand.MESSAGE_FORMAT_GUIDANCE;
    private final String missingSizeMessage = "Please enter a size." + AddDogCommand.MESSAGE_FORMAT_GUIDANCE;

    @Test
    public void parse_allFieldsPresent_success() throws Exception {
        Dog dog = new Dog(new DogName("Cookie"), new Breed("Golden Retriever"), new Size("Large"));

        assertParseSuccess(parser, validArguments, new AddDogCommand(dog));
        // Prefix order should not affect how values are associated with their fields.
        assertParseSuccess(parser, " s/Large b/Golden Retriever n/Cookie", new AddDogCommand(dog));
    }

    @Test
    public void parse_nameWithHyphenAndApostrophe_success() throws Exception {
        Dog dog = new Dog(new DogName("O'Brien-Sue"), new Breed("Golden Retriever"), new Size("Large"));

        assertParseSuccess(parser, " n/O'Brien-Sue b/Golden Retriever s/Large", new AddDogCommand(dog));
    }

    @Test
    public void parse_breedWithHyphen_success() throws Exception {
        Dog dog = new Dog(new DogName("Cookie"), new Breed("German Shepherd-Mix"), new Size("Large"));

        assertParseSuccess(parser, " n/Cookie b/German Shepherd-Mix s/Large", new AddDogCommand(dog));
    }

    @Test
    public void parse_missingOrBlankField_failure() {
        assertParseFailure(parser, " b/Golden Retriever s/Large", missingNameMessage);
        assertParseFailure(parser, " n/ b/Golden Retriever s/Large", missingNameMessage);

        assertParseFailure(parser, " n/Cookie s/Large", missingBreedMessage);
        assertParseFailure(parser, " n/Cookie b/ s/Large", missingBreedMessage);

        assertParseFailure(parser, " n/Cookie b/Golden Retriever", missingSizeMessage);
        assertParseFailure(parser, " n/Cookie b/Golden Retriever s/", missingSizeMessage);
    }

    @Test
    public void parse_invalidSize_failure() {
        assertParseFailure(parser, " n/Cookie b/Golden Retriever s/Tiny",
                Size.MESSAGE_CONSTRAINTS + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        String expectedMessage = "Multiple values specified for the following single-valued field(s): n/"
                + AddDogCommand.MESSAGE_FORMAT_GUIDANCE;

        assertParseFailure(parser, validArguments + " n/Milo", expectedMessage);
    }

    @Test
    public void parse_preamble_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddDogCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "unexpected" + validArguments, expectedMessage);
    }

    @Test
    public void parse_invalidNameOrBreed_failure() {
        assertParseFailure(parser, " n/Cookie! b/Golden Retriever s/Large",
                DogName.MESSAGE_CONSTRAINTS + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
        assertParseFailure(parser, " n/Cookie b/Poodle! s/Large",
                Breed.MESSAGE_CONSTRAINTS + AddDogCommand.MESSAGE_FORMAT_GUIDANCE);
    }

    @Test
    public void messageUsage_includesValidSizes() {
        assertTrue(AddDogCommand.MESSAGE_USAGE.contains("Valid sizes: small, medium, large, giant."));
    }
}
