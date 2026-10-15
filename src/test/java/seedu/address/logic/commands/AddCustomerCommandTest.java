package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;
import seedu.address.model.dog.Dog;
import seedu.address.model.person.Person;

public class AddCustomerCommandTest {

    private static final Customer ALICE = new Customer(1, new Name("Alice Tan"), new Phone("91234567"),
            new Email("alice@example.com"), new BackupContact("98765432"));

    @Test
    public void constructor_nullCustomer_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCustomerCommand(null));
    }

    @Test
    public void execute_customerAcceptedByModel_addsCustomerAndReportsSuccess() throws CommandException {
        ModelStubAcceptingCustomerAdded model = new ModelStubAcceptingCustomerAdded();

        CommandResult result = new AddCustomerCommand(ALICE).execute(model);

        assertEquals(AddCustomerCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(ALICE), model.customersAdded);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCustomerCommand(ALICE).execute(null));
    }

    @Test
    public void equalsAndToString() {
        AddCustomerCommand command = new AddCustomerCommand(ALICE);

        assertEquals(command, command);
        assertEquals(command, new AddCustomerCommand(ALICE));
        assertFalse(command.equals(new AddCustomerCommand(new Customer(2, new Name("Bob Lim"), new Phone("92345678"),
                new Email("bob@example.com"), new BackupContact("93456789")))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertEquals(AddCustomerCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}", command.toString());
    }

    private abstract static class ModelStub implements Model {
        @Override
        public void unlinkDogs(List<Integer> dogIds) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void unlinkCustomerDogs(int customerId) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void linkDogs(int customerId, List<Integer> dogIds) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Dog> getFilteredDogList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredDogList(Predicate<Dog> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override public ReadOnlyUserPrefs getUserPrefs() {
            throw unused();
        }
        @Override public GuiSettings getGuiSettings() {
            throw unused();
        }
        @Override public void setGuiSettings(GuiSettings guiSettings) {
            throw unused();
        }
        @Override public void setAddressBook(ReadOnlyAddressBook addressBook) {
            throw unused();
        }
        @Override public ReadOnlyAddressBook getAddressBook() {
            throw unused();
        }
        @Override public boolean hasPerson(Person person) {
            throw unused();
        }
        @Override public void deletePerson(Person person) {
            throw unused();
        }
        @Override public void addPerson(Person person) {
            throw unused();
        }
        @Override public void setPerson(Person target, Person editedPerson) {
            throw unused();
        }
        @Override public void deleteCustomer(Customer customer) {
            throw unused();
        }
        @Override public void setCustomer(Customer target, Customer editedCustomer) {
            throw unused();
        }
        @Override public ObservableList<Person> getFilteredPersonList() {
            throw unused();
        }
        @Override public ObservableList<Customer> getFilteredCustomerList() {
            throw unused();
        }
        @Override public void updateFilteredCustomerList(Predicate<Customer> predicate) {
            throw unused();
        }
        @Override public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw unused();
        }
        @Override public Dog addDog(Dog dog) {
            throw unused();
        }
        @Override public boolean hasDog(Dog dog) {
            throw unused();
        }
        @Override public void deleteDog(Dog dog) {
            throw unused();
        }
        @Override public void setDog(Dog target, Dog editedDog) {
            throw unused();
        }

        protected final AssertionError unused() {
            return new AssertionError("This method should not be called.");
        }
    }

    private static class ModelStubAcceptingCustomerAdded extends ModelStub {
        private final List<Customer> customersAdded = new ArrayList<>();

        @Override
        public boolean hasCustomer(Customer customer) {
            return customersAdded.stream().anyMatch(customer::isSameCustomer);
        }

        @Override
        public void addCustomer(Customer customer) {
            customersAdded.add(requireNonNull(customer));
        }
    }
}
