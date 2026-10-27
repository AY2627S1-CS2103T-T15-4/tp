package seedu.address.logic.commands;
//@@author Kenneth-Chia-unused
// Original category-search tests, superseded by searchd tests in PR #150;
// retained for authorship and reference.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;

/** Exercises search parsing and execution against the real model. */
public class SearchDogCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager(new AddressBook(), new UserPrefs());

    @Test
    public void execute_nameSearch_filtersMatchingDogsWithoutChangingRecords() throws Exception {
        Dog first = model.addDog(dog("Buddy"));
        Dog second = model.addDog(dog("Buddy"));
        Dog third = model.addDog(dog("Little Bud"));
        model.addDog(dog("Max"));
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals("3 dogs found.",
                execute("search dog n/bUd"));
        assertEquals(List.of(first, second, third), model.getFilteredDogList());
        assertEquals("1 dogs found.", execute("search dog n/little bud"));
        assertEquals(List.of(third), model.getFilteredDogList());
        assertEquals("0 dogs found.", execute("search dog n/little  bud"));
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_idSearch_usesPersistentIdAndDoesNotRetainPreviousSearch() throws Exception {
        Dog first = model.addDog(dog("Buddy"));
        Dog second = model.addDog(dog("Max"));
        assertEquals("1 dogs found.", execute("search dog n/Max"));
        assertEquals("1 dogs found.", execute("search dog id/" + first.getId()));
        assertEquals(List.of(first), model.getFilteredDogList());
        assertEquals("1 dogs found.", execute("search dog id/" + second.getId()));
        assertEquals(List.of(second), model.getFilteredDogList());
        assertEquals("0 dogs found.", execute("search dog id/2147483647"));
    }

    @Test
    public void execute_nonSequentialStoredId_doesNotUseListPosition() throws Exception {
        AddressBook storedBook = new AddressBook();
        Dog storedDog = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
        storedBook.addDogFromStorage(storedDog);
        model.setAddressBook(storedBook);
        assertEquals("1 dogs found.", execute("search dog id/42"));
        assertEquals("0 dogs found.", execute("search dog id/0"));
        assertEquals("0 dogs found.", execute("search dog id/1"));
    }

    @Test
    public void execute_emptyBook_returnsNoMatches() throws Exception {
        assertEquals("0 dogs found.", execute("search dog n/Buddy"));
        assertEquals("0 dogs found.", execute("search dog id/0"));
    }

    @Test
    public void parse_invalidInput_doesNotChangeRecords() {
        model.addDog(dog("Buddy"));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(ParseException.class, () -> parser.parseCommand("search dog id/-1"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void equals_sameSearchPredicateOnly() {
        SearchDogCommand command = new SearchDogCommand(new DogSearchPredicate("Buddy"));
        assertEquals(command, command);
        assertEquals(command, new SearchDogCommand(new DogSearchPredicate("Buddy")));
        assertNotEquals(command, new SearchDogCommand(new DogSearchPredicate("Max")));
        assertNotEquals(command, new SearchDogCommand(new DogSearchPredicate(42)));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
    }

    private String execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }

    private Dog dog(String name) {
        return new Dog(new DogName(name), new Breed("Poodle"), new Size("small"));
    }
}
