package seedu.address.model.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Contains tests for {@link Enrolment}. */
public class EnrolmentTest {
    @Test
    public void gettersAndEquality_behaveAsExpected() {
        Enrolment enrolment = new Enrolment(3, 7);
        Enrolment same = new Enrolment(3, 7);

        assertEquals(3, enrolment.getActivityId());
        assertEquals(7, enrolment.getDogId());
        assertEquals(enrolment, enrolment);
        assertEquals(enrolment, same);
        assertEquals(enrolment.hashCode(), same.hashCode());
        assertNotEquals(enrolment, new Enrolment(4, 7));
        assertNotEquals(enrolment, new Enrolment(3, 8));
        assertNotEquals(enrolment, null);
        assertNotEquals(enrolment, "enrolment");
    }
}
