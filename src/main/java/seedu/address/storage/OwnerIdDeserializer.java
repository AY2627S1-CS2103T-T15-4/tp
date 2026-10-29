package seedu.address.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/** Rejects coercion that could silently associate a dog with a different customer. */
class OwnerIdDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw context.mappingException("Dog owner ID must be an integer JSON number.");
        }
        return parser.getIntValue();
    }
}
