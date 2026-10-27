//@@author Kenneth-Chia-unused
// Excerpt from src/test/java/seedu/address/logic/commands/SummaryCommandTest.java at
// base 39316339edadcf928b135d07858d62a2d06ae177. This test was retired with the
// select workflow and is preserved here for authorship and reference.

    @Test
    public void execute_selectedDogOrCustomer_preservesSelectionAfterMutation() throws Exception {
        for (boolean selectDog : new boolean[] {true, false}) {
            Model model = mixedModel();
            Model expected = mixedModel();
            if (selectDog) {
                model.selectDog(9);
                expected.selectDog(9);
            } else {
                model.selectCustomer(1);
                expected.selectCustomer(1);
            }
            new SummaryCommand(CLOCK).execute(model);
            assertEquals(expected, model);
            model.unlinkDogs(List.of(9));
            expected.unlinkDogs(List.of(9));
            assertEquals(expected, model);
        }
    }
