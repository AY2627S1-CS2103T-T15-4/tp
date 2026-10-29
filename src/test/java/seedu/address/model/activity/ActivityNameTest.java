// @@author Kenneth-Chia
package seedu.address.model.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ActivityNameTest {
    @Test
    public void validation_nonblankText_preservesName() {
        String name = "Walk & play (group 2)";
        assertTrue(ActivityName.isValidName(name));
        assertEquals(name, new ActivityName(name).fullName);
        assertEquals(name, new ActivityName(name).toString());
        assertTrue(ActivityName.isValidName("散步"));
        assertFalse(ActivityName.isValidName(""));
        assertFalse(ActivityName.isValidName(" \t\n"));
        assertThrows(IllegalArgumentException.class, () -> new ActivityName(" "));
        assertThrows(NullPointerException.class, () -> new ActivityName(null));
        assertThrows(NullPointerException.class, () -> ActivityName.isValidName(null));
    }

    @Test
    public void equality_comparesExactText() {
        ActivityName name = new ActivityName("Walk");
        assertEquals(name, name);
        assertEquals(name, new ActivityName("Walk"));
        assertEquals(name.hashCode(), new ActivityName("Walk").hashCode());
        assertNotEquals(name, new ActivityName("walk"));
        assertNotEquals(name, null);
        assertNotEquals(name, "Walk");
    }
}
// @@author
