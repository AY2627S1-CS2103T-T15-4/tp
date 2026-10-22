package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.activity.Enrolment;

/** Contains tests for {@link JsonAdaptedEnrolment}. */
public class JsonAdaptedEnrolmentTest {
    @Test
    public void toModelType_validFields_returnsEnrolment() throws Exception {
        Enrolment expected = new Enrolment(3, 7, LocalDate.of(2028, 2, 29));
        assertEquals(expected, new JsonAdaptedEnrolment(expected).toModelType());
    }

    @Test
    public void toModelType_legacyFields_usesProvidedActivityDate() throws Exception {
        LocalDate legacyDate = LocalDate.of(2028, 2, 29);

        assertEquals(new Enrolment(3, 7, legacyDate), new JsonAdaptedEnrolment(3, 7).toModelType(legacyDate));
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedEnrolment(3, 7).toModelType());
    }

    @Test
    public void toModelType_missingOrNegativeFields_throwsException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(null, 7, LocalDate.of(2028, 2, 29)).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(3, null, LocalDate.of(2028, 2, 29)).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(-1, 7, LocalDate.of(2028, 2, 29)).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedEnrolment(3, -1, LocalDate.of(2028, 2, 29)).toModelType());
    }
}
