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

        int customerId = ParserUtil.parseCustomerId(argMultimap.getPreamble());
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);

        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(ParserUtil.parseCustomerName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            descriptor.setPhone(ParserUtil.parseCustomerPhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            descriptor.setEmail(ParserUtil.parseCustomerEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_BACKUP_CONTACT).isPresent()) {
            descriptor.setBackupContact(
                    ParserUtil.parseCustomerBackupContact(argMultimap.getValue(PREFIX_BACKUP_CONTACT).get()));
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCustomerCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCustomerCommand(customerId, descriptor);
    }
}
