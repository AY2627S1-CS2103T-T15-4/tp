//@@author its-shoutime
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandHelpTest {

    @Test
    public void getHelp_idConstraints_matchValidation() {
        String customerHelp = CommandHelp.getHelp(EditCustomerCommand.COMMAND_WORD).orElseThrow();
        String dogHelp = CommandHelp.getHelp(EditDogCommand.COMMAND_WORD).orElseThrow();
        String enrolHelp = CommandHelp.getHelp(EnrolDogCommand.COMMAND_WORD).orElseThrow();
        String deleteDogHelp = CommandHelp.getHelp(DeleteDogCommand.COMMAND_WORD).orElseThrow();

        assertTrue(customerHelp.contains("Customer and activity IDs range from 1 to 2147483647."));
        assertFalse(customerHelp.contains("customer IDs range from 0"));
        assertTrue(dogHelp.contains("Dog IDs range from 1 to 2147483647."));
        assertFalse(dogHelp.contains("Dog IDs range from 0"));
        assertTrue(deleteDogHelp.contains("deletedog 1"));
        assertTrue(enrolHelp.contains("d/2 3"));
        assertFalse(enrolHelp.contains("d/2,3"));
        assertTrue(dogHelp.contains("hyphens and apostrophes"));
    }
}
