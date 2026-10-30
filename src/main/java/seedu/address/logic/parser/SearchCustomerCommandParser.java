// @@author XanderGoh
package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.Phone;

/** Parses arguments for the {@code searchc} command. */
public class SearchCustomerCommandParser implements Parser<SearchCustomerCommand> {
    private static final Pattern FIELD_FORMAT = Pattern.compile(
            "(?<prefix>n|p|e|d|bc|i)/(?<value>.*?)(?=\\s+[A-Za-z]+/|$)");
    private static final String MESSAGE_SEARCH_ERROR = "Error in searching customers:\n";

    @Override
    public SearchCustomerCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String input = args.trim();
        if (input.isEmpty()) {
            throw invalidFormat();
        }
        String name = null;
        String phone = null;
        String email = null;
        String backupContact = null;
        Set<Integer> dogIds = null;
        Integer customerId = null;
        int end = 0;
        Matcher matcher = FIELD_FORMAT.matcher(input);
        while (matcher.find()) {
            if (!input.substring(end, matcher.start()).isBlank()) {
                throw invalidFormat();
            }
            String value = matcher.group("value").trim();
            String prefix = matcher.group("prefix");
            if (value.isEmpty() && !prefix.equals("p") && !prefix.equals("bc")) {
                throw invalidFormat();
            }
            switch (prefix) {
                case "n" -> {
                    if (name != null) {
                        throw invalidFormat();
                    }
                    name = value.replaceAll("\\s+", " ");
                }
                case "p" -> phone = parsePhone(value, phone, "phone (p/)");
                case "e" -> email = parseSingle(value, email);
                case "bc" -> backupContact = parsePhone(value, backupContact, "backup contact (bc/)");
                case "d" -> {
                    if (dogIds != null) {
                        throw invalidFormat();
                    }
                    dogIds = parseDogIds(value);
                }
                case "i" -> customerId = parseCustomerId(value, customerId);
                default -> throw invalidFormat();
            }
            end = matcher.end();
        }
        if (end == 0 || !input.substring(end).isBlank()) {
            throw invalidFormat();
        }
        return new SearchCustomerCommand(name, phone, email, backupContact, dogIds, customerId);
    }

    private static String parsePhone(String value, String existing, String field) throws ParseException {
        if (existing != null) {
            throw new ParseException(MESSAGE_SEARCH_ERROR + "Specify " + field + " only once.");
        }
        if (!Phone.isValidPhone(value)) {
            throw new ParseException(MESSAGE_SEARCH_ERROR + "Invalid " + field + ": " + phoneError(value));
        }
        return new Phone(value).value;
    }

    /** Explains why a contact query failed the shared phone validation. */
    private static String phoneError(String value) {
        if (value.isEmpty()) {
            return "Enter a complete phone number, e.g. 91234567 or +442012345678.";
        }
        if (!value.matches("[+0-9 -]+")) {
            return "Use digits and an optional leading +. Only spaces and hyphens are allowed between digits; "
                    + "letters, parentheses and other symbols are not allowed.";
        }
        if (!value.matches("\\+?[0-9](?:[0-9 -]*[0-9])?")) {
            return "Place + only at the start, immediately before a digit. "
                    + "Spaces and hyphens must be between digits.";
        }
        String unformatted = value.replace(" ", "").replace("-", "");
        if (unformatted.startsWith("+65")) {
            return "Singapore numbers must contain exactly 8 digits after +65, e.g. +6591234567.";
        }
        if (unformatted.startsWith("+0")) {
            return "The first digit after + must be 1-9.";
        }
        if (unformatted.startsWith("+")) {
            return "Enter the complete international number: + followed by 7 to 15 digits "
                    + "including the country code, e.g. +442012345678. Partial phone searches are not supported.";
        }
        return "Local numbers must contain exactly 8 digits without a country code, e.g. 91234567. "
                + "For international numbers, include + and the country code. "
                + "Partial phone searches are not supported.";
    }

    private static String parseSingle(String value, String existing) throws ParseException {
        if (existing != null) {
            throw invalidFormat();
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }

    private static Set<Integer> parseDogIds(String value) throws ParseException {
        Set<Integer> dogIds = new LinkedHashSet<>();
        for (String token : value.split("\\s+")) {
            try {
                int id = Integer.parseInt(token);
                if (id < 1) {
                    throw new NumberFormatException();
                }
                dogIds.add(id);
            } catch (NumberFormatException exception) {
                throw new ParseException(SearchCustomerCommand.MESSAGE_INVALID_DOG_ID, exception);
            }
        }
        return dogIds;
    }

    private static Integer parseCustomerId(String value, Integer existing) throws ParseException {
        if (existing != null) {
            throw invalidFormat();
        }
        try {
            int customerId = Integer.parseInt(value);
            if (customerId <= 0) {
                throw new NumberFormatException();
            }
            return customerId;
        } catch (NumberFormatException exception) {
            throw new ParseException(SearchCustomerCommand.MESSAGE_INVALID_CUSTOMER_ID, exception);
        }
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                SearchCustomerCommand.MESSAGE_USAGE));
    }
}
