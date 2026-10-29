package seedu.address.model.util;

import seedu.address.model.ReadOnlyWatchDog;
import seedu.address.model.WatchDog;
import seedu.address.model.customer.Customer;

/**
 * Contains utility methods for populating {@code WatchDog} with sample data.
 */
public class SampleDataUtil {
    public static Customer[] getSampleCustomers() {
        return new Customer[] {
            new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432"),
            new Customer(2, "John Doe", "81234567", "john@gmail.com", "81234568")
        };
    }

    public static ReadOnlyWatchDog getSampleWatchDog() {
        WatchDog sampleAb = new WatchDog();
        for (Customer sampleCustomer : getSampleCustomers()) {
            sampleAb.addCustomer(sampleCustomer);
        }
        return sampleAb;
    }

}
