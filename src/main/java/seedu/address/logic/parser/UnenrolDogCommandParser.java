package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.UnenrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an activity prefix followed by a comma-separated list of dog IDs. */
public class UnenrolDogCommandParser implements Parser<UnenrolDogCommand> {
    private static final Pattern UNENROL_FORMAT = Pattern.compile(
            "a/[0-9]+\\s+d/[0-9]+(?:\\s*,\\s*[0-9]+)*");

    @Override
    public UnenrolDogCommand parse(String args) throws ParseException {
        String input = args.strip();
        if (!UNENROL_FORMAT.matcher(input).matches()) {
            throw invalidFormat();
        }
        try {
            String[] fields = input.split("\\s+d/");
            int activityId = Integer.parseInt(fields[0].substring(2));
            List<Integer> dogIds = new ArrayList<>();
            for (String dogId : fields[1].split("\\s*,\\s*")) {
                dogIds.add(Integer.parseInt(dogId));
            }
            if (new HashSet<>(dogIds).size() != dogIds.size()) {
                throw invalidFormat();
            }
            return new UnenrolDogCommand(activityId, dogIds);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    UnenrolDogCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                UnenrolDogCommand.MESSAGE_USAGE));
    }
}
