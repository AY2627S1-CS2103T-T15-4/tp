package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_unconstrainedText_preservesValue() {
        for (String value : new String[] {"", " ", "Likes swimming!", "你好\nSecond line"}) {
            assertEquals(value, new Remark(value).value);
            assertEquals(value, new Remark(value).toString());
        }
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes swimming");
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Likes swimming")));
        assertFalse(remark.equals(new Remark("Likes baseball")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Likes swimming"));
    }

    @Test
    public void hashCode_equalRemarks_sameHashCode() {
        assertEquals(new Remark("Likes swimming").hashCode(), new Remark("Likes swimming").hashCode());
    }
}
