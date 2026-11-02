// @@author lilosy
package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_BACKUP_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.logic.commands.EditCustomerCommand;
import seedu.address.logic.commands.EditCustomerCommand.EditCustomerDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates an {@link EditCustomerCommand}.
 */
public class EditCustomerCommandParser implements Parser<EditCustomerCommand> {

    @Override
    public EditCustomerCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);

        int customerId = parseCustomerId(argMultimap.getPreamble());
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);

        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(parseField("name (n/)", ParserUtil::parseCustomerName,
                    argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            descriptor.setPhone(parseField("phone (p/)", ParserUtil::parseCustomerPhone,
                    argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            descriptor.setEmail(parseField("email (e/)", ParserUtil::parseCustomerEmail,
                    argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_BACKUP_CONTACT).isPresent()) {
            descriptor.setBackupContact(parseField("backup contact (bc/)", ParserUtil::parseCustomerBackupContact,
                    argMultimap.getValue(PREFIX_BACKUP_CONTACT).get()));
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCustomerCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCustomerCommand(customerId, descriptor);
    }

    /** Identifies the field while preserving its existing validation guidance. */
    private static <T> T parseField(String field, FieldParser<T> parser, String value) throws ParseException {
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            throw new ParseException("Invalid " + field + ": " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }

    private static int parseCustomerId(String value) throws ParseException {
        String trimmedId = value.trim();
        if (trimmedId.isEmpty()) {
            throw new ParseException("Customer ID is required. Enter a positive whole number.");
        }
        try {
            return ParserUtil.parseCustomerId(trimmedId);
        } catch (ParseException e) {
            throw new ParseException("Please provide a positive, valid customer ID.", e);
        }
    }
}
