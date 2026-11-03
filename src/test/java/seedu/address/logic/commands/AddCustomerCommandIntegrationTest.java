package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.WatchDogParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.customer.Customer;
import seedu.address.storage.JsonWatchDogStorage;

public class AddCustomerCommandIntegrationTest {

    private final WatchDogParser parser = new WatchDogParser();

    @Test
    public void execute_slashNamesAcceptedByAdd_areSearchable() throws Exception {
        for (String token : List.of("x/y", "abc/def", "X/Y", "s/o", "d/o", "d/1", "i/2", "d/abc",
                "x/y/z", "/", "\u9648/\u4f1f", "p/e")) {
            String name = token.equals("p/e") ? "Alexp/e Ali" : "Alex " + token + " Ali";
            Model model = new ModelManager();
            execute(model, name, "91234567", "alex@example.com");
            parser.parseCommand("searchc n/\"" + name + "\" i/1").execute(model);
            assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList(), name);
            if (!token.startsWith("d/") && !token.startsWith("i/")) {
                parser.parseCommand("searchc n/" + name).execute(model);
                assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList(), name);
            }
        }
    }

    @Test
    public void execute_invalidEmailPlacement_rejectsAddAndEditWithoutChangingRecords() throws Exception {
        Model model = new ModelManager();
        execute(model, "Alice Tan", "91234567", "alice@example.com");
        List<Customer> before = List.copyOf(model.getCustomerList());
        for (String email : List.of(".alice@example.com", "alice.@example.com", "alice..tan@example.com",
                "alice!work@example.com", "alice/bob@example.com", "alice&bob@example.com",
                "alice=work@example.com", "\"alice\"@example.com", "@example.com", "alice@")) {
            assertThrows(ParseException.class, () -> execute(model, "Bob Tan", "92345678", email));
            assertThrows(ParseException.class, () -> parser.parseCommand("editcustomer 1 e/" + email).execute(model));
            assertEquals(before, model.getCustomerList());
        }
    }

    @Test
    public void execute_emailSpecialCharacters_surviveEditingSearchingAndReload(@TempDir Path directory)
            throws Exception {
        Model model = new ModelManager();
        execute(model, "Alice Tan", "91234567", "alice+_work@example.com");
        assertEquals("alice+_work@example.com", model.getCustomerList().get(0).getEmail().value);
        parser.parseCommand("editcustomer 1 e/_alice+@example.com").execute(model);
        JsonWatchDogStorage storage = new JsonWatchDogStorage(directory.resolve("watchdog.json"));
        storage.saveWatchDog(model.getWatchDog());
        Model reloaded = new ModelManager(storage.readWatchDog().orElseThrow(), new UserPrefs());
        parser.parseCommand("searchc e/_ALICE+@EXAMPLE.COM").execute(reloaded);
        assertEquals(List.of(1), reloaded.getFilteredCustomerList().stream().map(Customer::getId).toList());
        assertEquals("_alice+@example.com", reloaded.getCustomerList().get(0).getEmail().value);
    }

    @Test
    public void execute_unicodeAndSlashNames_surviveEditingSearchingAndReload(@TempDir Path directory)
            throws Exception {
        Model model = new ModelManager();
        execute(model, "Ahmad s/o Ali", "91234567", "ahmad@example.com");
        execute(model, "Devi d/o Kumar", "92345678", "devi@example.com");
        execute(model, "\u9648\u4f1f", "93456789", "chen@example.com");
        assertEquals("Ahmad s/o Ali", model.getCustomerList().get(0).getName().fullName);
        assertEquals("91234567", model.getCustomerList().get(0).getPhone().value);
        for (String query : List.of("s/o", "Ahmad s/o Ali", "AHMAD S/O ALI")) {
            parser.parseCommand("searchc n/" + query).execute(model);
            assertEquals(List.of(1), model.getFilteredCustomerList().stream().map(Customer::getId).toList());
        }
        parser.parseCommand("searchc n/Devi d/o Kumar").execute(model);
        assertEquals(List.of(2), model.getFilteredCustomerList().stream().map(Customer::getId).toList());
        parser.parseCommand("adddog n/Buddy b/Poodle s/Small").execute(model);
        parser.parseCommand("link c/2 d/1").execute(model);
        parser.parseCommand("searchc n/Devi d/o Kumar d/1 i/2").execute(model);
        assertEquals(List.of(2), model.getFilteredCustomerList().stream().map(Customer::getId).toList());

        parser.parseCommand("editcustomer 1 n/Jose\u0301 O\u2019Neil").execute(model);
        assertEquals("Jos\u00e9 O\u2019Neil", model.getCustomerList().get(0).getName().fullName);
        assertEquals("ahmad@example.com", model.getCustomerList().get(0).getEmail().value);
        JsonWatchDogStorage storage = new JsonWatchDogStorage(directory.resolve("watchdog.json"));
        storage.saveWatchDog(model.getWatchDog());
        Model reloaded = new ModelManager(storage.readWatchDog().orElseThrow(), new UserPrefs());
        assertEquals(model.getCustomerList(), reloaded.getCustomerList());
        parser.parseCommand("searchc n/Devi d/o Kumar d/1").execute(reloaded);
        assertEquals(List.of(2), reloaded.getFilteredCustomerList().stream().map(Customer::getId).toList());
        for (String query : List.of("JOS\u00c9", "JOSE\u0301")) {
            parser.parseCommand("searchc n/" + query).execute(reloaded);
            assertEquals(List.of(1), reloaded.getFilteredCustomerList().stream().map(Customer::getId).toList());
        }
        parser.parseCommand("searchc n/\u9648").execute(reloaded);
        assertEquals(List.of(3), reloaded.getFilteredCustomerList().stream().map(Customer::getId).toList());
        assertTrue(reloaded.getCustomerList().stream()
                .anyMatch(customer -> customer.getName().fullName.equals("Devi d/o Kumar")));
    }

    @Test
    public void execute_exhaustedIds_reportsErrorAfterDeletionAndReload(@TempDir Path directory) throws Exception {
        WatchDog watchDog = new WatchDog();
        watchDog.setNextCustomerId(Integer.MAX_VALUE);
        Model model = new ModelManager(watchDog, new UserPrefs());
        execute(model, "Alice Tan", "91234567", "alice@example.com");
        assertEquals(Integer.MAX_VALUE, model.getCustomerList().get(0).getId());

        assertThrows(CommandException.class,
                "No more customer IDs are available", () -> execute(model, "Bob Lim", "92345678", "bob@example.com"));
        assertEquals(1, model.getCustomerList().size());

        model.deleteCustomer(model.getCustomerList().get(0));
        JsonWatchDogStorage storage = new JsonWatchDogStorage(directory.resolve("addressbook.json"));
        storage.saveWatchDog(model.getWatchDog());
        Model reloadedModel = new ModelManager(storage.readWatchDog().orElseThrow(), new UserPrefs());

        assertThrows(CommandException.class,
                "No more customer IDs are available", () -> execute(
                        reloadedModel, "Carol Lee", "93456789", "carol@example.com"));
        assertEquals(0, reloadedModel.getCustomerList().size());
    }

    @Test
    public void execute_assignsNonRepeatingIdsAndPreservesThemAfterReload(@TempDir Path directory) throws Exception {
        Model model = new ModelManager();

        execute(model, "Alice Tan", "91234567", "alice@example.com");
        execute(model, "Bob Lim", "92345678", "bob@example.com");
        Customer alice = model.getFilteredCustomerList().get(0);
        model.deleteCustomer(alice);
        execute(model, "Carol Lee", "93456789", "carol@example.com");

        assertEquals(List.of(2, 3), model.getFilteredCustomerList().stream().map(Customer::getId).toList());

        JsonWatchDogStorage storage = new JsonWatchDogStorage(directory.resolve("data").resolve("watchdog.json"));
        storage.saveWatchDog(model.getWatchDog());
        Model reloadedModel = new ModelManager(storage.readWatchDog().orElseThrow(), new UserPrefs());

        execute(reloadedModel, "David Tan", "94567890", "david@example.com");
        assertEquals(List.of(2, 3, 4), reloadedModel.getFilteredCustomerList().stream()
                .map(Customer::getId)
                .toList());
    }

    private void execute(Model model, String name, String phone, String email) throws Exception {
        parser.parseCommand("addcustomer n/" + name + " p/" + phone + " e/" + email + " bc/98765432")
                .execute(model);
    }
}
