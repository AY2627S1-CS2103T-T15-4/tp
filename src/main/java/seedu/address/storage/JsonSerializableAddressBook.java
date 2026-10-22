package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
@JsonIgnoreProperties("persons")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_CUSTOMER = "Customers list contains duplicate customer(s).";
    public static final String MESSAGE_DUPLICATE_DOG = "Dog list contains duplicate dog(s).";

    private final List<JsonAdaptedCustomer> customers = new ArrayList<>();
    private final List<JsonAdaptedDog> dogs = new ArrayList<>();
    private final List<JsonAdaptedActivity> activities = new ArrayList<>();
    private final List<JsonAdaptedEnrolment> enrolments = new ArrayList<>();
    private final Long nextActivityId;
    private final Integer nextDogId;
    private final Integer nextCustomerId;
    /** Constructs a serializable address book from JSON fields. */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("customers") List<JsonAdaptedCustomer> customers,
            @JsonProperty("dogs") List<JsonAdaptedDog> dogs,
            @JsonProperty("nextDogId") Integer nextDogId,
            @JsonProperty("nextCustomerId") Integer nextCustomerId,
            @JsonProperty("activities") List<JsonAdaptedActivity> activities,
            @JsonProperty("enrolments") List<JsonAdaptedEnrolment> enrolments,
            @JsonProperty("nextActivityId")
            @JsonDeserialize(using = NextActivityIdDeserializer.class) Long nextActivityId) {
        if (customers != null) {
            this.customers.addAll(customers);
        }
        if (dogs != null) {
            this.dogs.addAll(dogs);
        }
        if (activities != null) {
            this.activities.addAll(activities);
        }
        if (enrolments != null) {
            this.enrolments.addAll(enrolments);
        }
        this.nextActivityId = nextActivityId;
        this.nextDogId = nextDogId;
        this.nextCustomerId = nextCustomerId;
    }

    /** Backwards-compatible constructor for data without enrolments. */
    public JsonSerializableAddressBook(List<JsonAdaptedCustomer> customers, List<JsonAdaptedDog> dogs,
            Integer nextDogId, Integer nextCustomerId, List<JsonAdaptedActivity> activities, Long nextActivityId) {
        this(customers, dogs, nextDogId, nextCustomerId, activities, null, nextActivityId);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        customers.addAll(source.getCustomerList().stream()
                .map(JsonAdaptedCustomer::new)
                .collect(Collectors.toList()));
        dogs.addAll(source.getDogList().stream().map(JsonAdaptedDog::new).collect(Collectors.toList()));
        activities.addAll(source.getActivityList().stream().map(JsonAdaptedActivity::new).toList());
        enrolments.addAll(source.getEnrolmentList().stream().map(JsonAdaptedEnrolment::new).toList());
        nextActivityId = source.getNextActivityId();
        nextDogId = source.getNextDogId();
        nextCustomerId = source.getNextCustomerId();
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedCustomer jsonAdaptedCustomer : customers) {
            Customer customer = jsonAdaptedCustomer.toModelType();
            if (addressBook.hasCustomer(customer)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CUSTOMER);
            }
            addressBook.addCustomer(customer);
        }

        for (JsonAdaptedDog jsonAdaptedDog : dogs) {
            Dog dog = jsonAdaptedDog.toModelType();
            if (addressBook.hasDog(dog)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_DOG);
            }
            try {
                addressBook.addDogFromStorage(dog);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }

        if (nextDogId != null && nextDogId >= 0) {
            addressBook.setNextDogId(nextDogId);
        }
        if (nextCustomerId != null && nextCustomerId >= 1) {
            addressBook.setNextCustomerId(nextCustomerId);
        }

        for (JsonAdaptedActivity adapted : activities) {
            if (adapted == null) {
                throw new IllegalValueException("Activities list must not contain null entries.");
            }
            Activity activity = adapted.toModelType();
            try {
                addressBook.addActivityFromStorage(activity);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        if (nextActivityId != null) {
            try {
                addressBook.setNextActivityId(nextActivityId);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        for (JsonAdaptedEnrolment adapted : enrolments) {
            if (adapted == null) {
                throw new IllegalValueException("Enrolments list must not contain null entries.");
            }
            Activity activity = adapted.getActivityId() == null ? null : addressBook.getActivityList().stream()
                    .filter(candidate -> candidate.getId() == adapted.getActivityId())
                    .findFirst()
                    .orElse(null);
            if (activity == null) {
                throw new IllegalValueException("Invalid or duplicate enrolment.");
            }
            Enrolment enrolment = adapted.toModelType(activity.getStartDate());
            try {
                addressBook.addEnrolmentFromStorage(enrolment);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        return addressBook;
    }

}
