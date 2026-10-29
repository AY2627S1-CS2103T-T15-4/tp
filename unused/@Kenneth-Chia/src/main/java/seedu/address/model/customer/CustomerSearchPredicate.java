package seedu.address.model.customer;
//@@author Kenneth-Chia-unused
// Original category-search predicate, superseded by searchc in PR #150;
// retained for authorship and reference.

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;

/** Matches a customer's name substring or its exact persistent ID. */
public class CustomerSearchPredicate implements Predicate<Customer> {
    private final String nameQuery;
    private final Integer id;

    /** Creates a case-insensitive name substring search. */
    public CustomerSearchPredicate(String nameQuery) {
        this.nameQuery = requireNonNull(nameQuery).strip();
        checkArgument(!this.nameQuery.isEmpty(), "Name query cannot be blank");
        this.id = null;
    }

    /** Creates a search for a nonnegative persistent ID, not a displayed list index. */
    public CustomerSearchPredicate(int id) {
        checkArgument(id >= 0, "Customer ID must be nonnegative");
        this.id = id;
        this.nameQuery = null;
    }

    @Override
    public boolean test(Customer customer) {
        requireNonNull(customer);
        return id == null
                ? StringUtil.containsSubstringIgnoreCase(customer.getName().fullName, nameQuery)
                : customer.getId() == id;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CustomerSearchPredicate otherPredicate
                && Objects.equals(nameQuery, otherPredicate.nameQuery)
                && Objects.equals(id, otherPredicate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameQuery, id);
    }
}
