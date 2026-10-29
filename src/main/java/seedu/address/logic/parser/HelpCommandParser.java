package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.CommandHelp;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an optional command name for help. */
public class HelpCommandParser implements Parser<HelpCommand> {
    public static final String MESSAGE_UNKNOWN_TOPIC = "Unknown help topic: %s\n"
            + HelpCommand.MESSAGE_USAGE + "\nSupported commands: " + CommandHelp.getTopics();

    @Override
    public HelpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String topic = args.trim().replaceAll("\\s+", " ");
        if (topic.isEmpty()) {
            return new HelpCommand();
        }
        String help = CommandHelp.getHelp(topic).orElseThrow(() ->
                new ParseException(String.format(MESSAGE_UNKNOWN_TOPIC, topic)));
        return new HelpCommand(help);
    }
}
