package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

public class HelpCommandParserTest {
    private final WatchDogParser parser = new WatchDogParser();

    @Test
    public void parse_emptyTopic_opensOriginalHelpWindow() throws Exception {
        for (String input : new String[] {"help", "  help  ", "help\t"}) {
            assertEquals(new CommandResult(HelpCommand.SHOWING_HELP_MESSAGE, true, false),
                    parser.parseCommand(input).execute(new ModelManager()));
        }
    }

    @Test
    public void parse_supportedTopics_returnsFormattedHelpInResultSection() throws Exception {
        String[] topics = {"adddog", "addcustomer", "editdog", "editcustomer",
            "deletedog", "deletecustomer", "link", "unlink", "searchd",
            "searchc", "filterun", "addactivity", "editactivity", "deleteactivity", "searcha", "enrol",
            "unenrol", "today", "reset", "summary", "clear", "undo", "redo", "help", "exit"};
        for (String topic : topics) {
            CommandResult result = parser.parseCommand("help " + topic).execute(new ModelManager());
            assertFalse(result.isShowHelp(), topic);
            assertFalse(result.isExit(), topic);
            assertTrue(result.getFeedbackToUser().startsWith("Format: " + topic), topic);
            assertTrue(result.getFeedbackToUser().contains("\nExample: "), topic);
            assertTrue(result.getFeedbackToUser().contains("\nSpecifications: "), topic);
        }
    }

    @Test
    public void parse_commandTopic_acceptsSurroundingWhitespace() throws Exception {
        assertEquals(parser.parseCommand("help deletedog").execute(new ModelManager()),
                parser.parseCommand("  help \t deletedog  ").execute(new ModelManager()));
    }

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HelpCommandParser().parse(null));
    }

    @Test
    public void parse_unknownTopicOrCommandArguments_throwsHelpfulError() {
        for (String topic : new String[] {"3", "unknown", "Summary", "summary today", "adddog n/Cookie",
            "deletedog 1", "delete", "delete dog", "delete customer", "list cat", "list", "list dog",
            "list customer", "resetdisplay", "search", "select"}) {
            assertThrows(ParseException.class, String.format(HelpCommandParser.MESSAGE_UNKNOWN_TOPIC, topic), () ->
                    parser.parseCommand("help " + topic));
        }
    }
}
