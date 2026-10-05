package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/** Represents an optional remark attached to a person. */
public class Remark {
    public final String value;

    public Remark(String value) {
        requireNonNull(value);
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
