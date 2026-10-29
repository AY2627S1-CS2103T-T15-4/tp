//@@author its-shoutime-unused
// Historical selection-only excerpts from src/main/java/seedu/address/model/Model.java
// at base 39316339edadcf928b135d07858d62a2d06ae177. They were retired with the
// select workflow; these excerpts are not part of the active command surface.

record ModelState(ReadOnlyWatchDog watchDog, Predicate<Customer> customerPredicate,
        Predicate<Dog> dogPredicate, Predicate<Activity> activityPredicate,
        Integer selectedDogId, Integer selectedCustomerId) {
}

/** Shows the selected dog, its owner, and its current or future activities. */
void selectDog(int dogId);

/** Shows the selected customer, their dogs, and their current or future activities. */
void selectCustomer(int customerId);
