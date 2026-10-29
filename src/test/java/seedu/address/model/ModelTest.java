// @@author XanderGoh
package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests the default behavior supplied by the Model interface. */
public class ModelTest {
    @Test
    public void getDogList_defaultImplementation_returnsLiveUnmodifiableStoredDogs() {
        WatchDog watchDog = new WatchDog();
        Model model = (Model) Proxy.newProxyInstance(
                Model.class.getClassLoader(), new Class<?>[]{Model.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getWatchDog")) {
                        return watchDog;
                    }
                    return InvocationHandler.invokeDefault(proxy, method, args);
                });
        ObservableList<Dog> dogs = model.getDogList();
        assertTrue(dogs.isEmpty());

        Dog buddy = watchDog.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Dog max = watchDog.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("large")));
        assertEquals(List.of(buddy, max), dogs);
        assertThrows(UnsupportedOperationException.class, () -> dogs.remove(buddy));
        assertEquals(List.of(buddy, max), watchDog.getDogList());

        watchDog.removeDog(buddy);
        assertEquals(List.of(max), dogs);
    }
}
