package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditDogCommand;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link EditDogCommandParser}. */
public class EditDogCommandParserTest {
    private final EditDogCommandParser parser = new EditDogCommandParser();

    @Test
    public void parse_validFields_returnsCommand() throws Exception {
        EditDogCommand command = parser.parse("1 n/Milo b/Beagle s/Small");
        assertNotNull(command);
    }

    @Test
    public void parse_invalidId_includesFormatGuidance() {
        String message = "Please provide a positive, valid dog ID." + EditDogCommand.MESSAGE_FORMAT_GUIDANCE;
        assertParseFailure(parser, "abc n/Milo", message);
        assertParseFailure(parser, "-1 n/Milo", message);
        assertParseFailure(parser, "0 n/Milo", message);
        assertParseFailure(parser, " n/Milo", message);
    }

    @Test
    public void parse_duplicatePrefix_includesFormatGuidance() {
        String message = "Multiple values specified for the following single-valued field(s): n/"
                + EditDogCommand.MESSAGE_FORMAT_GUIDANCE;
        assertParseFailure(parser, "1 n/Milo n/Cookie", message);
    }

    @Test
    public void parse_noEditedFields_includesFormatGuidance() {
        assertParseFailure(parser, "1", EditDogCommand.MESSAGE_NOT_EDITED + EditDogCommand.MESSAGE_FORMAT_GUIDANCE);
    }

    @Test
    public void parse_invalidFieldValues_includeFormatGuidance() {
        String guidance = EditDogCommand.MESSAGE_FORMAT_GUIDANCE;
        assertParseFailure(parser, "1 n/Milo!", DogName.MESSAGE_CONSTRAINTS + guidance);
        assertParseFailure(parser, "1 b/Poodle!", Breed.MESSAGE_CONSTRAINTS + guidance);
        assertParseFailure(parser, "1 s/Tiny", Size.MESSAGE_CONSTRAINTS + guidance);
        assertParseFailure(parser, "1 s/", Size.MESSAGE_CONSTRAINTS + guidance);
    }
}
