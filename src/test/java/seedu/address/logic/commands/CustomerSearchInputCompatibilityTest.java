package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.WatchDogParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;

/** Exercises supported customer input categories through both command parsers. */
public class CustomerSearchInputCompatibilityTest {
    private final WatchDogParser parser = new WatchDogParser();

    @Test
    public void search_combinedNameAndEmail_requiresBothAndRefreshesAfterEdit() throws Exception {
        ModelManager model = new ModelManager();
        parser.parseCommand("addcustomer n/Alex d/1 Ali p/91234567 e/_alex+@example.com bc/98765432")
                .execute(model);
        parser.parseCommand("addcustomer n/Devi d/o Kumar p/92345678 e/devi+_work@example.com bc/98765432")
                .execute(model);
        List<Customer> records = List.copyOf(model.getCustomerList());
        parser.parseCommand("searchc n/\"Alex d/1 Ali\" e/devi+_work@example.com").execute(model);
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(records, model.getCustomerList());
        parser.parseCommand("searchc n/\"Alex d/1 Ali\" e/_ALEX+@EXAMPLE.COM").execute(model);
        assertEquals(List.of(records.get(0)), model.getFilteredCustomerList());
        parser.parseCommand("editcustomer 1 n/Jose\u0301 x/y Ali e/jose++@example.com").execute(model);
        assertEquals(List.of(), model.getFilteredCustomerList());
        parser.parseCommand("searchc n/\"JOS\u00c9 x/y Ali\" e/JOSE++@EXAMPLE.COM").execute(model);
        assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList());
        parser.parseCommand("searchc n/\"Jose\u0301 x/y Ali\" e/jose++@example.com").execute(model);
        assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList());
    }

    @Test
    public void search_malformedQuotedNamesAndDuplicateFields_preservesRecordsAndRelatedFilters() throws Exception {
        ModelManager model = new ModelManager();
        parser.parseCommand("addcustomer n/Alex d/1 Ali p/91234567 e/_alex+@example.com bc/98765432")
                .execute(model);
        parser.parseCommand("adddog n/Buddy b/Poodle s/small").execute(model);
        parser.parseCommand("link c/1 d/1").execute(model);
        parser.parseCommand("searchc n/\"Alex d/1 Ali\" e/_alex+@example.com").execute(model);
        List<Customer> records = List.copyOf(model.getCustomerList());
        var dogs = List.copyOf(model.getFilteredDogList());
        for (String query : List.of("n/\"Alex d/1 Ali", "n/\"\"", "n/\"Alex d/1 Ali\" n/Devi",
                "n/\"Alex d/1 Ali\" e/_alex+@example.com e/other@example.com",
                "n/\"Alex d/1 Ali\" d/abc", "e/_alex+@example.com x/value")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("searchc " + query).execute(model), query);
            assertEquals(records, model.getCustomerList());
            assertEquals(records, model.getFilteredCustomerList());
            assertEquals(dogs, model.getFilteredDogList());
        }
    }

    @Test
    public void search_namesAcceptedByAdd_matchesUnicodePunctuationAndSlashTokens() throws Exception {
        List<String> names = new ArrayList<>(List.of("Alice", "123", "Alice  2", "Jean--Luc", "O''Neil",
                "O\u2019Neil", "Jos\u00e9 Tan", "Jose\u0301 Tan", "\u9648\u4f1f", "\u0905\u092e\u093f\u0924",
                "\u0623\u062d\u0645\u062f", "\ud801\udc00 Alex", "Alex /", "Alex // Ali", "Alex x/y/z Ali",
                "Ahmad s/o Ali", "Devi d/o Kumar", "Alex d/1 Ali", "Alex i/2 Ali", "Alex d/o/o Ali",
                "Alex bcdef/value Ali", "Alexp/value Ali", "Alexe/value Ali", "Alexn/value Ali"));
        for (char prefix = 'a'; prefix <= 'z'; prefix++) {
            if (prefix != 'n' && prefix != 'p' && prefix != 'e') {
                names.add("Alex " + prefix + "/value Ali");
            }
            names.add("Alex " + Character.toUpperCase(prefix) + "/value Ali");
        }
        for (String name : names) {
            assertSearchable(name, "alice+_work@example.com");
        }
    }

    @Test
    public void search_emailsAcceptedByAdd_matchesEverySupportedPunctuationPlacementAndDomainForm() throws Exception {
        List<String> locals = new ArrayList<>(List.of("alice", "123", "alice.tan", "a.b.c", "+_'-",
                "alice._work", "alice+.work", "'o''neil'"));
        String punctuation = "+_'-";
        for (char first : punctuation.toCharArray()) {
            locals.add(first + "alice");
            locals.add("alice" + first);
            for (char second : punctuation.toCharArray()) {
                locals.add("alice" + first + second + "work");
            }
        }
        for (String local : locals) {
            for (String domain : List.of("example.com", "gmail", "sub.my-domain.edu.sg", "12.a1")) {
                assertSearchable("Devi d/o Kumar", local + "@" + domain);
            }
        }
    }

    /** Uses a distractor to verify matching rather than merely accepting query syntax. */
    private void assertSearchable(String name, String email) throws Exception {
        ModelManager model = new ModelManager();
        parser.parseCommand("addcustomer n/" + name + " p/91234567 e/" + email + " bc/98765432").execute(model);
        parser.parseCommand("addcustomer n/Unrelated Customer p/92345678 e/other@example.org bc/98765432")
                .execute(model);
        for (String query : List.of("n/\"" + name + "\"", "e/" + email,
                "n/\"" + name + "\" e/" + email.toUpperCase(Locale.ROOT),
                "e/" + email + " n/\"" + name + "\" i/1 p/91234567 bc/98765432")) {
            parser.parseCommand("searchc " + query).execute(model);
            assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList(), query);
        }
    }
}
