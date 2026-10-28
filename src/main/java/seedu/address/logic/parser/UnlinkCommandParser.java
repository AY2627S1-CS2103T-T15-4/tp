package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses either one customer field or one space-separated dog ID field. */
public class UnlinkCommandParser implements Parser<UnlinkCommand> {
    private static final Pattern UNLINK_FORMAT = Pattern.compile("(?:c/[0-9]+|d/[0-9]+(?:\\s+[0-9]+)*)");

    @Override
    public UnlinkCommand parse(String args) throws ParseException {
        String input = args.strip();
        if (!UNLINK_FORMAT.matcher(input).matches()) {
            throw invalidFormat();
        }
        String[] fields = input.split("\\s+");
        try {
            if (input.startsWith("c/")) {
                return new UnlinkCommand(Integer.parseInt(fields[0].substring(2)));
            }
            List<Integer> dogIds = new ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                dogIds.add(Integer.parseInt(i == 0 ? fields[i].substring(2) : fields[i]));
            }
            if (new HashSet<>(dogIds).size() != dogIds.size()) {
                throw invalidFormat();
            }
            return new UnlinkCommand(dogIds);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlinkCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlinkCommand.MESSAGE_USAGE));
    }
}
