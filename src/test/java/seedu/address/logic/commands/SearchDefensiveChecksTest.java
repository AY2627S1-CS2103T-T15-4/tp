package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.DeleteDogCommandParser;
import seedu.address.logic.parser.FilterUnlinkedCommandParser;
import seedu.address.logic.parser.SearchActivityCommandParser;
import seedu.address.logic.parser.SearchCustomerCommandParser;
import seedu.address.logic.parser.SearchDogCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivitySearchPredicate;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;

/** Regression tests for command boundaries and retained search inputs. */
public class SearchDefensiveChecksTest {
    @Test
    public void customerSearch_invalidDogSets_rejectInvalidCallerInput() {
        for (Integer id : new Integer[]{null, 0, -1}) {
            Set<Integer> ids = new HashSet<>();
            ids.add(id);
            assertThrows(IllegalArgumentException.class, () ->
                    new SearchCustomerCommand(null, null, null, null, ids));
            assertThrows(IllegalArgumentException.class, () ->
                    new CustomerSearchPredicate(null, null, null, null, ids, List.of()));
        }
        assertThrows(NullPointerException.class, () ->
                new CustomerSearchPredicate(null, null, null, null, Set.of(1), null));
    }

    @Test
    public void predicates_withoutFields_rejectNullRecords() {
        assertThrows(NullPointerException.class, () ->
                new CustomerSearchPredicate(null, null, null, null, null, List.of()).test(null));
        assertThrows(NullPointerException.class, () -> new DogSearchPredicate(null, null, null, null).test(null));
        assertThrows(NullPointerException.class, () -> new ActivitySearchPredicate(null, null, null, null).test(null));
    }

    @Test
    public void parsers_outOfRangeIds_rejectWithoutLeakingNumberFormatException() {
        for (String id : new String[]{"2147483648", "-2147483649", "1 2"}) {
            assertThrows(ParseException.class, () -> new DeleteDogCommandParser().parse(id));
            assertThrows(ParseException.class, () -> new SearchDogCommandParser().parse("i/" + id));
            assertThrows(ParseException.class, () -> new SearchCustomerCommandParser().parse("i/" + id));
            if (!id.equals("1 2")) {
                assertThrows(ParseException.class, () -> new SearchCustomerCommandParser().parse("d/" + id));
            }
            assertThrows(ParseException.class, () -> new SearchActivityCommandParser().parse("i/" + id));
        }
    }

    @Test
    public void constructors_nonpositiveIds_rejectInvalidCallerInput() {
        for (int id : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new SearchCustomerCommand(null, null, null, null, null, id));
            assertThrows(IllegalArgumentException.class, () ->
                    new SearchActivityCommand(null, null, null, null, id));
        }
    }

    @Test
    public void parsers_nullArguments_rejectCallerError() {
        assertThrows(NullPointerException.class, () -> new DeleteDogCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new SearchCustomerCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new SearchDogCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new SearchActivityCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new FilterUnlinkedCommandParser().parse(null));
    }

    @Test
    public void customerSearch_mutatingCallerIds_doesNotChangeCommandOrActiveFilter() throws Exception {
        ModelManager model = new ModelManager();
        Customer customer = new Customer(1, "Anna", "91234567", "anna@example.com", "98765432");
        model.addCustomer(customer);
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(customer.getId(), List.of(dog.getId()));
        Set<Integer> ids = new HashSet<>(Set.of(dog.getId()));
        SearchCustomerCommand command = new SearchCustomerCommand(null, null, null, null, ids);
        ids.add(-1);

        command.execute(model);

        assertEquals(List.of(customer), model.getFilteredCustomerList());
        model.unlinkDogs(List.of(dog.getId()));
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void customerPredicate_copiesIdsButRetainsLiveDogList() throws Exception {
        ModelManager model = new ModelManager();
        Customer customer = new Customer(1, "Anna", "91234567", "anna@example.com", "98765432");
        model.addCustomer(customer);
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(customer.getId(), List.of(dog.getId()));
        Set<Integer> ids = new HashSet<>(Set.of(dog.getId()));
        CustomerSearchPredicate predicate = new CustomerSearchPredicate(
                null, null, null, null, ids, model.getDogList());
        ids.add(null);

        assertTrue(predicate.test(customer));
        model.unlinkDogs(List.of(dog.getId()));
        assertTrue(!predicate.test(customer));
    }

    @Test
    public void deleteDog_hiddenDogAndRepeatedDeletion_preservesRemainingRecords() throws Exception {
        ModelManager model = new ModelManager();
        Dog hidden = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Dog visible = model.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("Small")));
        new SearchDogCommand(null, null, null, visible.getId()).execute(model);

        new DeleteDogCommand(hidden.getId()).execute(model);
        assertThrows(CommandException.class, () -> new DeleteDogCommand(hidden.getId()).execute(model));

        assertEquals(List.of(visible), model.getDogList());
        assertEquals(List.of(visible), model.getFilteredDogList());
    }

    @Test
    public void filterUnlinked_emptyModelAndNullModel_handlesBoundaries() throws Exception {
        for (boolean dogs : new boolean[]{true, false}) {
            ModelManager model = new ModelManager();
            new FilterUnlinkedCommand(dogs).execute(model);
            assertTrue(model.getFilteredDogList().isEmpty());
            assertTrue(model.getFilteredCustomerList().isEmpty());
            assertTrue(model.getFilteredActivityList().isEmpty());
            assertThrows(NullPointerException.class, () -> new FilterUnlinkedCommand(dogs).execute(null));
        }
    }
}
