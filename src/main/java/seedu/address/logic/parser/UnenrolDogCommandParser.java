package seedu.address.logic.parser;

import seedu.address.logic.commands.UnenrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the activity, occurrence date, and dog IDs for an unenrol command.
 */
public class UnenrolDogCommandParser implements Parser<UnenrolDogCommand> {
    @Override
    public UnenrolDogCommand parse(String args) throws ParseException {
        DatedDogIdsParser.ParsedDogIds parsed = DatedDogIdsParser.parse(
                args, UnenrolDogCommand.MESSAGE_FORMAT_GUIDANCE);
        return new UnenrolDogCommand(parsed.activityId(), parsed.date(), parsed.dogIds());
    }
}
