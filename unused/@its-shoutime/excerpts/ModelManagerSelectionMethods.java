//@@author its-shoutime-unused
// Historical selection-only excerpts from src/main/java/seedu/address/model/ModelManager.java at base 39316339edadcf928b135d07858d62a2d06ae177.
// These methods were retired with the select workflow; active model code remains unchanged.

private Integer selectedDogId;
private Integer selectedCustomerId;

// Selection state was included in createState/restoreState and compared by equals.
return new ModelState(new AddressBook(addressBook), activeCustomerPredicate, activeDogPredicate,
        activeActivityPredicate, selectedDogId, selectedCustomerId);
selectedDogId = state.selectedDogId();
selectedCustomerId = state.selectedCustomerId();

    public void selectDog(int dogId) {
        assert dogId >= 1 : "Dog ID must be positive";
        selectedCustomerId = null;
        selectedDogId = dogId;
        applySelectedDog();
    }

    public void selectCustomer(int customerId) {
        assert customerId >= 1 : "Customer ID must be positive";
        selectedDogId = null;
        selectedCustomerId = customerId;
        applySelectedCustomer();
    }

    private void clearSelection() {
        selectedDogId = null;
        selectedCustomerId = null;
    }

    private void refreshSelectedDog() {
        applySelectedDog();
    }

    private void refreshDisplayedRecords() {
        if (selectedDogId != null) {
            refreshSelectedDog();
            return;
        }
        if (selectedCustomerId != null) {
            applySelectedCustomer();
            return;
        }
        if (activeCustomerPredicate != null) {
            filteredCustomers.setPredicate(null);
            filteredCustomers.setPredicate(activeCustomerPredicate);
        }
        if (activeDogPredicate != null) {
            filteredDogs.setPredicate(null);
            filteredDogs.setPredicate(activeDogPredicate);
        }
        if (activeActivityPredicate != null) {
            filteredActivities.setPredicate(null);
            filteredActivities.setPredicate(activeActivityPredicate);
        }
    }

    private void applySelectedDog() {
        int dogId = selectedDogId;
        filteredDogs.setPredicate(dog -> dog.getId() == dogId);
        filteredCustomers.setPredicate(customer -> ownerIdOf(dogId)
                .filter(ownerId -> ownerId == customer.getId()).isPresent());
        filteredActivities.setPredicate(activity -> isVisibleForSelectedDog(dogId, activity));
    }

    private Optional<Integer> ownerIdOf(int dogId) {
        for (Dog dog : addressBook.getDogList()) {
            if (dog.getId() == dogId) {
                return dog.getOwnerId();
            }
        }
        return Optional.empty();
    }

    private boolean isVisibleForSelectedDog(int dogId, Activity activity) {
        requireNonNull(activity);
        LocalDate today = LocalDate.now();
        return addressBook.getEnrolmentList().stream().anyMatch(enrolment ->
                enrolment.getDogId() == dogId && enrolment.getActivityId() == activity.getId()
                        && !enrolment.getDate().isBefore(today) && activity.occursOn(enrolment.getDate()));
    }

    private void applySelectedCustomer() {
        int customerId = selectedCustomerId;
        filteredCustomers.setPredicate(customer -> customer.getId() == customerId);
        filteredDogs.setPredicate(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customerId).isPresent());
        filteredActivities.setPredicate(activity -> isVisibleForSelectedCustomer(customerId, activity));
    }

    private boolean isVisibleForSelectedCustomer(int customerId, Activity activity) {
        return addressBook.getDogList().stream()
                .filter(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customerId).isPresent())
                .anyMatch(dog -> isVisibleForSelectedDog(dog.getId(), activity));
    }
