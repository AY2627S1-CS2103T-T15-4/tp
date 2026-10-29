// @@author lilosy
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BACKUP_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.AddCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

/**
 * Parses input arguments and creates an {@link AddCustomerCommand}.
 */
public class AddCustomerCommandParser implements Parser<AddCustomerCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code AddCustomerCommand}
     * and returns an {@code AddCustomerCommand} for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public AddCustomerCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCustomerCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);
        List<String> missingFields = new ArrayList<>();
        if (argMultimap.getValue(PREFIX_NAME).isEmpty()) {
            missingFields.add("name");
        }
        if (argMultimap.getValue(PREFIX_PHONE).isEmpty()) {
            missingFields.add("phone");
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isEmpty()) {
            missingFields.add("email");
        }
        if (argMultimap.getValue(PREFIX_BACKUP_CONTACT).isEmpty()) {
            missingFields.add("backup contact");
        }
        if (!missingFields.isEmpty()) {
            throw new ParseException("Missing required field(s): " + String.join(", ", missingFields) + ".\n"
                    + AddCustomerCommand.MESSAGE_USAGE);
        }

        Name name = parseField("name", ParserUtil::parseCustomerName, argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = parseField("phone", ParserUtil::parseCustomerPhone, argMultimap.getValue(PREFIX_PHONE).get());
        Email email = parseField("email", ParserUtil::parseCustomerEmail, argMultimap.getValue(PREFIX_EMAIL).get());
        BackupContact backupContact = parseField("backup contact", ParserUtil::parseCustomerBackupContact,
                argMultimap.getValue(PREFIX_BACKUP_CONTACT).get());

        Customer customer = new Customer(name, phone, email, backupContact);

        return new AddCustomerCommand(customer);
    }

    /** Parses a field and identifies it in any validation error. */
    private static <T> T parseField(String fieldName, FieldParser<T> parser, String value) throws ParseException {
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            throw new ParseException("Invalid " + fieldName + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }

}
