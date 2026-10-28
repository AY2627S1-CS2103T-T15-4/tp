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
import seedu.address.logic.commands.EditActivityCommand;
import seedu.address.logic.commands.EditActivityCommand.EditActivityDescriptor;
import seedu.address.logic.commands.EditDogCommand;
import seedu.address.logic.commands.EditDogCommand.EditDogDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterUnlinkedCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ResetDisplayCommand;
import seedu.address.logic.commands.SearchActivityCommand;
import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_retiredSearch_throwsUnknownCommand() {
        for (String input : new String[] {"search", "search n/Alex", "search id/0",
            "search dog n/Buddy", "search customer n/Alex", "  search id/42  "}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_summary_acceptsSurroundingWhitespace() throws Exception {
        assertEquals(new SummaryCommand(), parser.parseCommand("summary"));
        assertEquals(new SummaryCommand(), parser.parseCommand(" \t summary \t "));
    }

    @Test
    public void parseCommand_summaryWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SummaryCommand.MESSAGE_USAGE);
        for (String input : new String[] {"summary today", "summary 1", "summary date/2026-10-24"}) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_today_acceptsOnlyCommandWord() throws Exception {
        assertEquals(new TodayCommand(), parser.parseCommand("today"));
        assertEquals(new TodayCommand(), parser.parseCommand("  today  "));
        assertThrows(ParseException.class, () -> parser.parseCommand("today tomorrow"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("Today"));
    }

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
    public void parseCommand_selectWithoutCategoryIsRejected() {
        for (String input : new String[] {"select", "select rabbit 1"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
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
    public void parseCommand_editActivity() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));

        assertEquals(new EditActivityCommand(3, descriptor),
                parser.parseCommand("editactivity 3 n/Training"));
    }

    @Test
    public void parseCommand_editActivityInvalidArgs_failure() {
        assertThrows(ParseException.class, () -> parser.parseCommand("editactivity invalid n/Training"));
    }

    @Test
    public void parseCommand_searchCustomer() throws Exception {
        assertEquals(new SearchCustomerCommand("anna", null, null, null, null, 1),
                parser.parseCommand("searchc n/anna i/1"));
    }

    @Test
    public void parseCommand_searchDog() throws Exception {
        assertEquals(new SearchDogCommand("lala", null, null, null),
                parser.parseCommand("searchd n/lala"));
    }

    @Test
    public void parseCommand_searchUnlinked() throws Exception {
        assertEquals(new FilterUnlinkedCommand(true), parser.parseCommand("filterun   dog"));
        assertEquals(new FilterUnlinkedCommand(false), parser.parseCommand("filterun customer"));
    }

    @Test
    public void parseCommand_searchActivity() throws Exception {
        assertEquals(new SearchActivityCommand("walk", null, null),
                parser.parseCommand("searcha n/walk"));
    }

    @Test
    public void parseCommand_oldSearchNames_throwUnknownCommand() {
        for (String input : new String[] {"filtera n/walk", "filterc n/anna", "filterd n/lala",
            "searchun dog"}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
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
    public void parseCommand_undoRedo_acceptOnlyCommandWord() throws Exception {
        assertTrue(parser.parseCommand("  undo  ") instanceof UndoCommand);
        assertTrue(parser.parseCommand("  redo  ") instanceof RedoCommand);
        String undoError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.COMMAND_WORD);
        String redoError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.COMMAND_WORD);
        assertThrows(ParseException.class, undoError, () -> parser.parseCommand("undo 1"));
        assertThrows(ParseException.class, redoError, () -> parser.parseCommand("redo 1"));
    }

    @Test
    public void parseCommand_retiredListCommands_throwUnknownCommand() {
        for (String input : new String[] {"list", "list dog", "list customer", "list dogs", "list customers",
            "  list   dog  ", "list   customer", "list dog 1", "list customer extra"}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_resetDisplay_acceptsOnlyCommandWord() throws Exception {
        assertTrue(parser.parseCommand("resetdisplay") instanceof ResetDisplayCommand);
        assertTrue(parser.parseCommand("  resetdisplay  ") instanceof ResetDisplayCommand);
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ResetDisplayCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("resetdisplay tomorrow"));
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
    public void parseCommand_removedActivityCommands_throwUnknownCommand() {
        for (String input : new String[] {"mark 1", "unmark 1"}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
