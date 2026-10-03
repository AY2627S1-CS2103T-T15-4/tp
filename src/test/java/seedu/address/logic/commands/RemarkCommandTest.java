package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_addRemarkUnfilteredList_success() {
        assertRemarkSuccess(INDEX_FIRST_PERSON, VALID_REMARK_AMY);
    }

    @Test
    public void execute_replaceRemark_success() {
        setFirstPersonRemark(VALID_REMARK_AMY);
        assertRemarkSuccess(INDEX_FIRST_PERSON, VALID_REMARK_BOB);
    }

    @Test
    public void execute_deleteRemark_success() {
        setFirstPersonRemark(VALID_REMARK_AMY);
        assertRemarkSuccess(INDEX_FIRST_PERSON, "");
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        // Filter to the second person, who now has displayed index 1.
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess(INDEX_FIRST_PERSON, VALID_REMARK_AMY);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(invalidIndex, new Remark(VALID_REMARK_AMY)),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY)),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyFilteredList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY)),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_BOB))));
    }

    private void setFirstPersonRemark(String remark) {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(original, new PersonBuilder(original).withRemark(remark).build());
    }

    private void assertRemarkSuccess(Index index, String remark) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        int addressBookIndex = model.getAddressBook().getPersonList().indexOf(original);
        Person edited = new PersonBuilder(original).withRemark(remark).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String message = remark.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;

        assertCommandSuccess(new RemarkCommand(index, new Remark(remark)), model,
                String.format(message, Messages.format(edited)), expectedModel);
        // Verify every stored remark explicitly.
        for (int i = 0; i < expectedModel.getAddressBook().getPersonList().size(); i++) {
            assertEquals(expectedModel.getAddressBook().getPersonList().get(i).getRemark(),
                    model.getAddressBook().getPersonList().get(i).getRemark());
        }
        assertEquals(new Remark(remark), model.getAddressBook().getPersonList().get(addressBookIndex).getRemark());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }
}
