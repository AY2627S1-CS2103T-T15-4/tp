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
        return toCheck.getId() >= 1 && internalList.stream().anyMatch(toCheck::isSameDog);
    }

    /** Adds a dog that does not already exist. */
    public void add(Dog toAdd) {
        requireNonNull(toAdd);
        if (toAdd.getId() < 1) {
            throw new IllegalArgumentException("Dog ID must be positive.");
        }
        if (contains(toAdd)) {
            throw new DuplicateDogException();
        }
        internalList.add(toAdd);
    }

    // @@author XanderGoh
    /** Removes a dog that exists in this list. */
    public void remove(Dog toRemove) {
        requireNonNull(toRemove);
        internalList.remove(toRemove);
    }
    // @@author

    /** Replaces a dog while preserving uniqueness by persistent ID. */
    public void setDog(Dog target, Dog editedDog) {
        requireAllNonNull(target, editedDog);
        int targetIndex = -1;
        for (int i = 0; i < internalList.size(); i++) {
            if (internalList.get(i).isSameDog(target)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex < 0) {
            throw new IllegalArgumentException("Dog does not exist");
        }

        if (!target.isSameDog(editedDog)) {
            throw new IllegalArgumentException("Edited dog must preserve its ID");
        }

        for (int i = 0; i < internalList.size(); i++) {
            if (i != targetIndex && internalList.get(i).isSameDog(editedDog)) {
                throw new DuplicateDogException();
            }
        }

        internalList.set(targetIndex, editedDog);
    }

    /** Replaces the contents of this list with the given dogs. */
    public void setDogs(List<Dog> dogs) {
        requireAllNonNull(dogs);
        if (dogs.stream().anyMatch(dog -> dog.getId() < 1)) {
            throw new IllegalArgumentException("Dog IDs must be positive.");
        }
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
        return internalUnmodifiableList.iterator();
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
