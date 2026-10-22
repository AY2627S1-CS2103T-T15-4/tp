package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddActivityCommand;
import seedu.address.logic.commands.AddCustomerCommand;
import seedu.address.logic.commands.AddDogCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteActivityCommand;
import seedu.address.logic.commands.DeleteCustomerCommand;
import seedu.address.logic.commands.EditActivityCommand;
import seedu.address.logic.commands.EditCustomerCommand;
import seedu.address.logic.commands.EditDogCommand;
import seedu.address.logic.commands.EnrolDogCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterActivityCommand;
import seedu.address.logic.commands.FilterCustomerCommand;
import seedu.address.logic.commands.FilterDogCommand;
import seedu.address.logic.commands.FilterUnlinkedCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListDogCommand;
import seedu.address.logic.commands.MarkActivityCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SelectDogCommand;
import seedu.address.logic.commands.UnenrolDogCommand;
import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.commands.UnmarkActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Pattern LIST_DOG_COMMAND_FORMAT = Pattern.compile("list\\s+dog");
    private static final Pattern DELETE_DOG_COMMAND_FORMAT = Pattern.compile(
            "delete\\s+dog(?=\\s|$)(?<arguments>.*)");
    private static final Pattern DELETE_CUSTOMER_COMMAND_FORMAT = Pattern.compile("delete\\s+customer(?:\\s+.*)?");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        if (LIST_DOG_COMMAND_FORMAT.matcher(userInput.trim()).matches()) {
            return new ListDogCommand();
        }
        Matcher deleteDogMatcher = DELETE_DOG_COMMAND_FORMAT.matcher(userInput.trim());
        if (deleteDogMatcher.matches()) {
            return new DeleteDogCommandParser().parse(deleteDogMatcher.group("arguments"));
        }
        if (DELETE_CUSTOMER_COMMAND_FORMAT.matcher(userInput.trim()).matches()) {
            String arguments = userInput.trim().substring(DeleteCustomerCommand.COMMAND_WORD.length());
            return new DeleteCustomerCommandParser().parse(arguments);
        }
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddActivityCommand.COMMAND_WORD -> new AddActivityCommandParser().parse(arguments);
            case FilterActivityCommand.COMMAND_WORD -> new FilterActivityCommandParser().parse(arguments);
            case AddCustomerCommand.COMMAND_WORD -> new AddCustomerCommandParser().parse(arguments);
            case AddDogCommand.COMMAND_WORD -> new AddDogCommandParser().parse(arguments);
            case DeleteActivityCommand.COMMAND_WORD -> new DeleteActivityCommandParser().parse(arguments);
            case EditActivityCommand.COMMAND_WORD -> new EditActivityCommandParser().parse(arguments);
            case EditCustomerCommand.COMMAND_WORD -> new EditCustomerCommandParser().parse(arguments);
            case EditDogCommand.COMMAND_WORD -> new EditDogCommandParser().parse(arguments);
            case FilterDogCommand.COMMAND_WORD -> new FilterDogCommandParser().parse(arguments);
            case FilterCustomerCommand.COMMAND_WORD -> new FilterCustomerCommandParser().parse(arguments);
            case FilterUnlinkedCommand.COMMAND_WORD -> new FilterUnlinkedCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case SearchCommand.COMMAND_WORD -> new SearchCommandParser().parse(arguments);
            case SelectDogCommand.COMMAND_WORD -> new SelectCommandParser().parse(arguments);
            case LinkCommand.COMMAND_WORD -> new LinkCommandParser().parse(arguments);
            case UnlinkCommand.COMMAND_WORD -> new UnlinkCommandParser().parse(arguments);
            case EnrolDogCommand.COMMAND_WORD -> new EnrolDogCommandParser().parse(arguments);
            case UnenrolDogCommand.COMMAND_WORD -> new UnenrolDogCommandParser().parse(arguments);
            case MarkActivityCommand.COMMAND_WORD -> new MarkActivityCommandParser().parse(arguments);
            case UnmarkActivityCommand.COMMAND_WORD -> new UnmarkActivityCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
