package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BACKUP_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.stream.Stream;

import seedu.address.logic.commands.AddCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

/** Parses input arguments and creates an {@link AddCustomerCommand}. */
public class AddCustomerCommandParser implements Parser<AddCustomerCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code AddCustomerCommand}
     * and returns an {@code AddCustomerCommand} for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCustomerCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCustomerCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_BACKUP_CONTACT);
        Name name = ParserUtil.parseCustomerName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parseCustomerPhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseCustomerEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        BackupContact backupContact =
                ParserUtil.parseCustomerBackupContact(argMultimap.getValue(PREFIX_BACKUP_CONTACT).get());

        Customer customer = new Customer(name, phone, email, backupContact);

        return new AddCustomerCommand(customer);
    }

    /**
     * Returns true if all specified prefixes have values in the given {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
