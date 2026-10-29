//@@author its-shoutime-unused
// Historical excerpts from src/test/java/seedu/address/model/ModelManagerTest.java at base 39316339edadcf928b135d07858d62a2d06ae177.
// These selection tests were retired when select was removed from the final product.

    @Test
    public void restoreState_restoresSelectedRecord() {
        modelManager.addCustomer(ALICE);
        Dog buddy = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        modelManager.selectDog(buddy.getId());
        Model.ModelState selectedDogState = modelManager.createState();
        modelManager.selectCustomer(ALICE.getId());
        Model.ModelState selectedCustomerState = modelManager.createState();

        modelManager.restoreState(selectedDogState);
        assertEquals(List.of(buddy), modelManager.getFilteredDogList());
        assertEquals(List.of(), modelManager.getFilteredCustomerList());

        modelManager.restoreState(selectedCustomerState);
        assertEquals(List.of(ALICE), modelManager.getFilteredCustomerList());
        assertEquals(List.of(), modelManager.getFilteredDogList());
        assertThrows(NullPointerException.class, () -> modelManager.restoreState(null));
    }

    @Test
    public void selectDog_unownedDog_refreshesAfterAddingCustomer() {
        WatchDog watchDog = new WatchDog();
        watchDog.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        watchDog.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large")));
        ModelManager first = new ModelManager(watchDog, new UserPrefs());
        ModelManager second = new ModelManager(watchDog, new UserPrefs());

        first.selectDog(42);
        first.addCustomer(ALICE);

        assertFalse(first.equals(second));
        assertTrue(first.getFilteredCustomerList().isEmpty());
        assertEquals(1, first.getFilteredDogList().size());
    }

    @Test
    public void selectDog_showsOnlyItsTodayAndFutureEnrolments() {
        WatchDog watchDog = new WatchDog();
        LocalDate today = LocalDate.now();
        Dog selectedDog = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
        Dog otherDog = new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large"));
        watchDog.addDogFromStorage(selectedDog);
        watchDog.addDogFromStorage(otherDog);
        Activity pastActivity = watchDog.addActivity(new ActivityName("Past"), today.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity todayActivity = watchDog.addActivity(new ActivityName("Today"), today,
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        Activity futureActivity = watchDog.addActivity(new ActivityName("Future"), today.plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity otherDogActivity = watchDog.addActivity(new ActivityName("Other dog"), today,
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        watchDog.addEnrolmentFromStorage(new Enrolment(pastActivity.getId(), selectedDog.getId(),
                today.minusDays(1)));
        watchDog.addEnrolmentFromStorage(new Enrolment(todayActivity.getId(), selectedDog.getId(), today));
        watchDog.addEnrolmentFromStorage(new Enrolment(futureActivity.getId(), selectedDog.getId(),
                today.plusDays(1)));
        watchDog.addEnrolmentFromStorage(new Enrolment(otherDogActivity.getId(), otherDog.getId(), today));
        modelManager = new ModelManager(watchDog, new UserPrefs());

        modelManager.selectDog(selectedDog.getId());

        assertEquals(List.of(todayActivity, futureActivity), modelManager.getFilteredActivityList());
    }

    @Test
    public void equals_differentSelectedDog_returnsFalse() {
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(new Customer(7, "Alex", "91234567", "alex@example.com", "98765432"));
        watchDog.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large")));
        watchDog.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        ModelManager first = new ModelManager(watchDog, new UserPrefs());
        ModelManager second = new ModelManager(watchDog, new UserPrefs());

        first.selectDog(42);
        second.selectDog(99);

        assertFalse(first.equals(second));

        ModelManager sameFiltersFirst = new ModelManager();
        ModelManager sameFiltersSecond = new ModelManager();
        sameFiltersFirst.selectDog(2);
        sameFiltersSecond.selectDog(3);
        assertFalse(sameFiltersFirst.equals(sameFiltersSecond));
        assertThrows(AssertionError.class, () -> first.selectDog(0));
    }

    @Test
    public void equals_differentSelectedCustomer_returnsFalse() {
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(new Customer(42, "Alex", "91234567", "alex@example.com", "98765432"));
        watchDog.addCustomer(new Customer(43, "Bob", "81234567", "bob@example.com", "87654321"));
        ModelManager first = new ModelManager(watchDog, new UserPrefs());
        ModelManager second = new ModelManager(watchDog, new UserPrefs());

        first.selectCustomer(42);
        second.selectCustomer(43);

        assertFalse(first.equals(second));

        ModelManager sameFiltersFirst = new ModelManager();
        ModelManager sameFiltersSecond = new ModelManager();
        sameFiltersFirst.selectCustomer(2);
        sameFiltersSecond.selectCustomer(3);
        assertFalse(sameFiltersFirst.equals(sameFiltersSecond));
        assertThrows(AssertionError.class, () -> first.selectCustomer(0));
    }
