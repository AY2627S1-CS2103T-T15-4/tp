//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
// Excerpt from RecordListPanelTest.java at 333692c5.
    @Test
    public void panel_globalSearch_showsBothCategoriesAndSwitchesToSpecificSearch() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(new Customer(42, "Buddy Tan", "91234567", "buddy@example.com", "98765432"));
            book.addDogFromStorage(BUDDY);
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));

            new SearchCommand("bud").execute(model);
            assertEquals(2, list.getItems().size());
            assertEquals("Buddy Tan", label(list.getItems().get(0).get(), "name"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));
            executeCommand(new SearchCustomerCommand(new CustomerSearchPredicate("bud")), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy Tan", label(list.getItems().get(0).get(), "name"));
            new SearchCommand(42).execute(model);
            assertEquals(2, list.getItems().size());
            executeCommand(new SearchDogCommand(new DogSearchPredicate(42)), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            new SearchCommand("missing").execute(model);
            assertTrue(list.getItems().isEmpty());
            assertEquals(book, model.getAddressBook());
        });
    }
