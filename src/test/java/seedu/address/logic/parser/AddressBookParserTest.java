package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddDogCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCustomerCommand;
import seedu.address.logic.commands.DeleteDogCommand;
import seedu.address.logic.commands.EditDogCommand;
import seedu.address.logic.commands.EditDogCommand.EditDogDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SelectDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_addDog() throws Exception {
        Dog dog = new Dog(new DogName("Cookie"), new Breed("Golden Retriever"), new Size("Large"));
        AddDogCommand command = (AddDogCommand) parser.parseCommand(
                "adddog n/Cookie b/Golden Retriever s/Large");

        assertEquals(new AddDogCommand(dog), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_selectDog() throws Exception {
        SelectDogCommand command = (SelectDogCommand) parser.parseCommand("select dog 21");
        assertEquals(new SelectDogCommand(21), command);
    }

    @Test
    public void parseCommand_deleteDog() throws Exception {
        DeleteDogCommand command = (DeleteDogCommand) parser.parseCommand("delete dog 12");
        assertEquals(new DeleteDogCommand(12), command);
    }

    @Test
    public void parseCommand_deleteDogWithMultipleSpaces() throws Exception {
        DeleteDogCommand command = (DeleteDogCommand) parser.parseCommand("delete  dog   12");
        assertEquals(new DeleteDogCommand(12), command);
    }

    @Test
    public void parseCommand_deleteCustomer() throws Exception {
        DeleteCustomerCommand command = (DeleteCustomerCommand) parser.parseCommand("delete customer 21");
        assertEquals(new DeleteCustomerCommand(21), command);
    }

    @Test
    public void parseCommand_editDog() throws Exception {
        EditDogDescriptor descriptor = new EditDogDescriptor();
        descriptor.setSize(new Size("Medium"));

        EditDogCommand command = (EditDogCommand) parser.parseCommand("editdog 1 s/Medium");
        assertEquals(new EditDogCommand(1, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " customer") instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + "   customer  ") instanceof ListCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_retiredPersonCommands_throwUnknownCommand() {
        for (String input : new String[] {"add n/Alice p/91234567", "edit 1 n/Alice", "delete 1", "find Alice"}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
