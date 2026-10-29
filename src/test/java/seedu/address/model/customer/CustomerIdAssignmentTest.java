package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.WatchDog;

public class CustomerIdAssignmentTest {

    @Test
    public void addCustomer_assignsIncreasingIdsWithoutReusingDeletedId() {
        WatchDog watchDog = new WatchDog();
        Customer firstCustomer = watchDog.addCustomer(customer("Alice"));
        Customer secondCustomer = watchDog.addCustomer(customer("Bob"));

        watchDog.removeCustomer(firstCustomer);
        Customer thirdCustomer = watchDog.addCustomer(customer("Carol"));

        assertEquals(1, firstCustomer.getId());
        assertEquals(2, secondCustomer.getId());
        assertEquals(3, thirdCustomer.getId());
    }

    @Test
    public void watchDogCopy_preservesNextCustomerId() {
        WatchDog original = new WatchDog();
        Customer firstCustomer = original.addCustomer(customer("Alice"));
        original.removeCustomer(firstCustomer);

        WatchDog copy = new WatchDog(original);
        Customer secondCustomer = copy.addCustomer(customer("Bob"));

        assertEquals(2, secondCustomer.getId());
    }

    @Test
    public void addCustomer_existingId_advancesCounterForLaterUnassignedCustomer() {
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(new Customer(7, "Stored", "91234567", "stored@example.com", "98765432"));

        Customer assignedCustomer = watchDog.addCustomer(customer("New Customer"));

        assertEquals(8, assignedCustomer.getId());
    }

    @Test
    public void resetData_zeroNextCustomerId_recomputesPositiveId() {
        WatchDog watchDog = new WatchDog(new WatchDog() {
            @Override
            public long getNextCustomerId() {
                return 0;
            }
        });

        assertEquals(1, watchDog.getNextCustomerId());
        assertEquals(1, watchDog.addCustomer(customer("Alice")).getId());
    }

    @Test
    public void addCustomer_maximumId_exhaustsIdsWithoutReusingDeletedId() {
        WatchDog watchDog = new WatchDog();
        watchDog.setNextCustomerId(Integer.MAX_VALUE);

        Customer lastCustomer = watchDog.addCustomer(customer("Alice"));

        assertEquals(Integer.MAX_VALUE, lastCustomer.getId());
        assertEquals((long) Integer.MAX_VALUE + 1, watchDog.getNextCustomerId());
        assertThrows(IllegalStateException.class,
                "No more customer IDs are available", () -> watchDog.addCustomer(customer("Bob")));
        assertEquals(1, watchDog.getCustomerList().size());

        watchDog.removeCustomer(lastCustomer);
        WatchDog copy = new WatchDog(watchDog);
        assertThrows(IllegalStateException.class,
                "No more customer IDs are available", () -> copy.addCustomer(customer("Carol")));
        assertEquals(0, copy.getCustomerList().size());
    }

    @Test
    public void setNextCustomerId_aboveExhaustedCounter_rejectsWithoutChangingCounter() {
        WatchDog watchDog = new WatchDog();
        long exhaustedCounter = (long) Integer.MAX_VALUE + 1;
        watchDog.setNextCustomerId(exhaustedCounter);

        assertThrows(IllegalArgumentException.class, "Invalid next customer ID.", () ->
                watchDog.setNextCustomerId(exhaustedCounter + 1));

        assertEquals(exhaustedCounter, watchDog.getNextCustomerId());
        assertThrows(IllegalStateException.class, "No more customer IDs are available", () ->
                watchDog.addCustomer(customer("Alice")));
    }

    @Test
    public void setNextCustomerId_existingCustomerId_rejectsWithoutChangingCounter() {
        WatchDog watchDog = new WatchDog();
        Customer firstCustomer = watchDog.addCustomer(customer("Alice"));

        assertThrows(IllegalArgumentException.class, "Invalid next customer ID.", () ->
                watchDog.setNextCustomerId(firstCustomer.getId()));

        assertEquals(2, watchDog.addCustomer(customer("Bob")).getId());
    }

    @Test
    public void setNextCustomerId_rejectsNonPositiveId() {
        WatchDog watchDog = new WatchDog();

        assertThrows(IllegalArgumentException.class, () -> watchDog.setNextCustomerId(0));
        watchDog.setNextCustomerId(10);
        assertEquals(10, watchDog.addCustomer(customer("Alice")).getId());
    }

    private static Customer customer(String name) {
        return new Customer(new Name(name), new Phone("91234567"), new Email("test@example.com"),
                new BackupContact("98765432"));
    }
}
