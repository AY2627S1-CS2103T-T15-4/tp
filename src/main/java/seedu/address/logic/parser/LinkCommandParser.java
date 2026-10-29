package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses one customer field followed by one space-separated dog ID field. */
public class LinkCommandParser implements Parser<LinkCommand> {
    private static final Pattern LINK_FORMAT = Pattern.compile("c/[0-9]+\\s+d/[0-9]+(?:\\s+[0-9]+)*");

    @Override
    public LinkCommand parse(String args) throws ParseException {
        String input = args.strip();
        if (!LINK_FORMAT.matcher(input).matches()) {
            throw invalidFormat();
        }
        String[] fields = input.split("\\s+");
        try {
            int customerId = Integer.parseInt(fields[0].substring(2));
            if (customerId <= 0) {
                throw invalidFormat();
            }
            List<Integer> dogIds = new ArrayList<>();
            for (int i = 1; i < fields.length; i++) {
                dogIds.add(Integer.parseInt(i == 1 ? fields[i].substring(2) : fields[i]));
            }
            if (new HashSet<>(dogIds).size() != dogIds.size()) {
                throw invalidFormat();
            }
            return new LinkCommand(customerId, dogIds);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LinkCommand.MESSAGE_USAGE), e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LinkCommand.MESSAGE_USAGE));
    }
}
