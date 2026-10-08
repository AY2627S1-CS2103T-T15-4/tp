package seedu.address.model;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import seedu.address.model.person.Person;
import seedu.address.model.dog.Dog;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    default ObservableList<Dog> getDogList() {
        return FXCollections.emptyObservableList();
    }

}
