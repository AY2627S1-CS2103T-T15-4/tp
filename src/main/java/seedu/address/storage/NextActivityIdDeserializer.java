// @@author Kenneth-Chia
package seedu.address.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/** Preserves the exact integer counter without changing Jackson's other model mappings. */
class NextActivityIdDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw context.mappingException("Next activity ID must be an integer JSON number.");
        }
        return parser.getLongValue();
    }
}
// @@author
