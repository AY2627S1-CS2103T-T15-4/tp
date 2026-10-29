// @@author Kenneth-Chia
package seedu.address.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/** Rejects coercion of fractional or textual activity IDs before domain validation. */
class ActivityIdDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw context.mappingException("Activity ID must be an integer JSON number.");
        }
        return parser.getIntValue();
    }
}
