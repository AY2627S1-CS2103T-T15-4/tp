package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.dog.exceptions.DuplicateDogException;

/** A list of dogs that does not allow duplicate persistent IDs or null values. */
public class UniqueDogList implements Iterable<Dog> {
    private final ObservableList<Dog> internalList = FXCollections.observableArrayList();
    private final ObservableList<Dog> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** Returns true if a dog with the same persistent ID exists. */
    public boolean contains(Dog toCheck) {
        requireNonNull(toCheck);
        return toCheck.getId() >= 0 && internalList.stream().anyMatch(toCheck::isSameDog);
    }

    /** Adds a dog that does not already exist. */
    public void add(Dog toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateDogException();
        }
        internalList.add(toAdd);
    }

    /** Replaces the contents of this list with the given dogs. */
    public void setDogs(List<Dog> dogs) {
        requireAllNonNull(dogs);
        for (int i = 0; i < dogs.size(); i++) {
            for (int j = i + 1; j < dogs.size(); j++) {
                if (dogs.get(i).isSameDog(dogs.get(j))) {
                    throw new DuplicateDogException();
                }
            }
        }
        internalList.setAll(dogs);
    }

    /** Returns an unmodifiable view of the dogs. */
    public ObservableList<Dog> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Dog> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UniqueDogList otherUniqueDogList)) {
            return false;
        }
        return internalList.equals(otherUniqueDogList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }
}
