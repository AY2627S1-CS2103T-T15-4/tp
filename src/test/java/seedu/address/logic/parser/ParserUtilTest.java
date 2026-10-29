package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class ParserUtilTest {
    @Test
    public void parseCustomerId_validBoundariesAndWhitespace_success() throws Exception {
        assertEquals(1, ParserUtil.parseCustomerId(" 1 "));
        assertEquals(Integer.MAX_VALUE, ParserUtil.parseCustomerId("2147483647"));
    }

    @Test
    public void parseCustomerId_invalidOrOutOfRange_throwsParseException() {
        for (String input : new String[] {"", "0", "-1", "1 a", "2147483648"}) {
            assertThrows(ParseException.class, "Please provide a valid positive customer ID", () ->
                    ParserUtil.parseCustomerId(input));
        }
    }

    @Test
    public void parseCustomerFields_validValues_trimWhitespace() throws Exception {
        assertEquals(new Name("Alice Tan"), ParserUtil.parseCustomerName(" \tAlice Tan\n"));
        assertEquals(new Phone("91234567"), ParserUtil.parseCustomerPhone(" 91234567 "));
        assertEquals(new Email("alice@example.com"), ParserUtil.parseCustomerEmail(" alice@example.com "));
        assertEquals(new BackupContact("98765432"), ParserUtil.parseCustomerBackupContact(" 98765432 "));
    }

    @Test
    public void parseCustomerFields_invalidValues_reportFieldConstraints() {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseCustomerName("R@chel"));
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseCustomerPhone("12a"));
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseCustomerEmail("invalid"));
        assertThrows(ParseException.class, BackupContact.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseCustomerBackupContact("12"));
    }

    @Test
    public void parseDogFields_validValues_trimWhitespace() throws Exception {
        assertEquals(new DogName("Buddy"), ParserUtil.parseDogName(" \tBuddy\n"));
        assertEquals(new Breed("Golden Retriever"), ParserUtil.parseBreed(" Golden Retriever "));
        assertEquals(new Size("small"), ParserUtil.parseSize(" small "));
    }

    @Test
    public void parseDogFields_invalidValues_reportFieldConstraints() {
        assertThrows(ParseException.class, DogName.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseDogName("R@chel"));
        assertThrows(ParseException.class, Breed.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseBreed("Poodle!"));
        assertThrows(ParseException.class, Size.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseSize("tiny"));
    }

    @Test
    public void parseFields_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCustomerId(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCustomerName(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCustomerPhone(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCustomerEmail(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCustomerBackupContact(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDogName(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseBreed(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseSize(null));
    }
}
