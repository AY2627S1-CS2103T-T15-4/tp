package seedu.address.logic.parser;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.UnenrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an activity prefix followed by a comma-separated list of dog IDs. */
public class UnenrolDogCommandParser implements Parser<UnenrolDogCommand> {
    private static final Pattern UNENROL_FORMAT = Pattern.compile(
            "a/-?[0-9]+\\s+date/[^\\s]+\\s+d/(?:-?[0-9]+(?:\\s*,\\s*-?[0-9]+)*)?");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String MESSAGE_INVALID_DATE =
            "Date must be a real calendar date in YYYY-MM-DD format.";
    private static final String MESSAGE_INVALID_IDS =
            "Activity IDs must be whole numbers from 1 to 2147483647; dog IDs from 0 to 2147483647.";

    @Override
    public UnenrolDogCommand parse(String args) throws ParseException {
        String input = args.strip();
        if (!UNENROL_FORMAT.matcher(input).matches()) {
            throw invalidFormat();
        }
        try {
            String[] fields = input.split("\\s+d/", -1);
            String[] activityFields = activityIdAndDate(fields[0]);
            int activityId = Integer.parseInt(activityFields[0]);
            LocalDate date = LocalDate.parse(activityFields[1], DATE_FORMAT);
            if (fields[1].isBlank()) {
                throw new ParseException("Provide at least one dog ID.");
            }
            List<Integer> dogIds = new ArrayList<>();
            for (String dogId : fields[1].split("\\s*,\\s*")) {
                dogIds.add(Integer.parseInt(dogId));
            }
            if (activityId <= 0 || dogIds.stream().anyMatch(dogId -> dogId < 0)) {
                throw new ParseException(MESSAGE_INVALID_IDS);
            }
            HashSet<Integer> seenDogIds = new HashSet<>();
            for (int dogId : dogIds) {
                if (!seenDogIds.add(dogId)) {
                    throw new ParseException("Dog ID " + dogId
                            + " is listed more than once. Provide each dog ID only once.");
                }
            }
            return new UnenrolDogCommand(activityId, date, dogIds);
        } catch (NumberFormatException e) {
            throw new ParseException(MESSAGE_INVALID_IDS, e);
        } catch (DateTimeException e) {
            throw new ParseException(MESSAGE_INVALID_DATE, e);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException("Invalid unenrol format. Use: unenrol a/ACTIVITY_ID date/YYYY-MM-DD "
                + "d/DOG_ID[,DOG_ID...]. Example: unenrol a/1 date/2026-10-27 d/2,3");
    }

    private String[] activityIdAndDate(String activityField) {
        String[] fields = activityField.split("\\s+date/");
        return new String[] {fields[0].substring(2), fields[1]};
    }
}
