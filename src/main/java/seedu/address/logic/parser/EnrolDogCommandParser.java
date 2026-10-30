package seedu.address.logic.parser;

import seedu.address.logic.commands.EnrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the activity, occurrence date, and dog IDs for an enrol command.
 */
public class EnrolDogCommandParser implements Parser<EnrolDogCommand> {
    @Override
    public EnrolDogCommand parse(String args) throws ParseException {
        DatedDogIdsParser.ParsedDogIds parsed = DatedDogIdsParser.parse(
                args, EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE);
        return new EnrolDogCommand(parsed.activityId(), parsed.date(), parsed.dogIds());
    }
}
