package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests the full-data, dated enrolment query independently of command formatting. */
public class EnrolledDogsQueryTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 24);
    private static final Dog FIRST = dog(9);
    private static final Dog SECOND = dog(4);
    private static final Dog OTHER_DATE = dog(7);

    @Test
    public void getDogsEnrolledOn_deduplicatesByIdInDogListOrderDespiteFilters() {
        Model model = modelWithEnrolments();
        model.updateFilteredDogList(unused -> false);
        model.updateFilteredCustomerList(unused -> false);
        model.updateFilteredActivityList(unused -> false);

        assertEquals(List.of(FIRST, SECOND), model.getDogsEnrolledOn(DATE));
        assertEquals(List.of(OTHER_DATE), model.getDogsEnrolledOn(DATE.minusDays(1)));
        assertEquals(List.of(OTHER_DATE), model.getDogsEnrolledOn(DATE.plusDays(1)));
        assertEquals(List.of(), model.getFilteredDogList());
    }

    @Test
    public void getDogsEnrolledOn_noMatches_returnsEmptyList() {
        assertEquals(List.of(), new ModelManager().getDogsEnrolledOn(DATE));
        assertEquals(List.of(), modelWithEnrolments().getDogsEnrolledOn(DATE.plusDays(2)));
    }

    @Test
    public void getDogsEnrolledOn_resultIsUnmodifiableSnapshot() throws Exception {
        Model model = modelWithEnrolments();
        List<Dog> snapshot = model.getDogsEnrolledOn(DATE);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(OTHER_DATE));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, OTHER_DATE));

        model.unenrolDogs(1, DATE, List.of(SECOND.getId()));
        Dog renamed = new Dog(FIRST.getId(), new DogName("Renamed"), FIRST.getBreed(), FIRST.getSize());
        model.setDog(FIRST, renamed);
        model.deleteDog(SECOND);
        assertEquals(List.of(FIRST, SECOND), snapshot);
        assertEquals(List.of(renamed), model.getDogsEnrolledOn(DATE));
        model.setWatchDog(new WatchDog());
        assertEquals(List.of(FIRST, SECOND), snapshot);
        assertEquals(List.of(), model.getDogsEnrolledOn(DATE));
    }

    @Test
    public void getDogsEnrolledOn_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelWithEnrolments().getDogsEnrolledOn(null));
    }

    private static Model modelWithEnrolments() {
        WatchDog book = new WatchDog();
        book.addDogFromStorage(FIRST);
        book.addDogFromStorage(SECOND);
        book.addDogFromStorage(OTHER_DATE);
        book.addActivity(new ActivityName("Walk"), DATE.minusDays(1), LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.DAILY);
        book.addActivity(new ActivityName("Training"), DATE, LocalTime.of(9, 0),
                LocalTime.of(10, 0), Recurrence.ONCE);
        // Enrolment insertion and ID order both differ from stored dog order. Names are identical.
        book.addEnrolmentFromStorage(new Enrolment(1, SECOND.getId(), DATE));
        book.addEnrolmentFromStorage(new Enrolment(2, FIRST.getId(), DATE));
        book.addEnrolmentFromStorage(new Enrolment(1, FIRST.getId(), DATE));
        book.addEnrolmentFromStorage(new Enrolment(1, OTHER_DATE.getId(), DATE.minusDays(1)));
        book.addEnrolmentFromStorage(new Enrolment(1, OTHER_DATE.getId(), DATE.plusDays(1)));
        return new ModelManager(book, new UserPrefs());
    }

    private static Dog dog(int id) {
        return new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
    }
}
