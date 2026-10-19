package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.activity.Enrolment;

/** Contains tests for {@link JsonAdaptedEnrolment}. */
public class JsonAdaptedEnrolmentTest {
    @Test
    public void toModelType_validFields_returnsEnrolment() throws Exception {
        Enrolment expected = new Enrolment(3, 7);
        assertEquals(expected, new JsonAdaptedEnrolment(expected).toModelType());
    }

    @Test
    public void toModelType_missingOrNegativeFields_throwsException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(null, 7).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(3, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(-1, 7).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(3, -1).toModelType());
    }
}
