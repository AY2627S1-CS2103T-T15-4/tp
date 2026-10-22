package seedu.address.model.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Contains tests for {@link Enrolment}. */
public class EnrolmentTest {
    @Test
    public void gettersAndEquality_behaveAsExpected() {
        Enrolment enrolment = new Enrolment(3, 7, LocalDate.of(2028, 2, 29));
        Enrolment same = new Enrolment(3, 7, LocalDate.of(2028, 2, 29));

        assertEquals(3, enrolment.getActivityId());
        assertEquals(7, enrolment.getDogId());
        assertEquals(LocalDate.of(2028, 2, 29), enrolment.getDate());
        assertEquals(enrolment, enrolment);
        assertEquals(enrolment, same);
        assertEquals(enrolment.hashCode(), same.hashCode());
        assertNotEquals(enrolment, new Enrolment(4, 7, LocalDate.of(2028, 2, 29)));
        assertNotEquals(enrolment, new Enrolment(3, 8, LocalDate.of(2028, 2, 29)));
        assertNotEquals(enrolment, new Enrolment(3, 7, LocalDate.of(2028, 3, 1)));
        assertNotEquals(enrolment, null);
        assertNotEquals(enrolment, "enrolment");
    }
}
