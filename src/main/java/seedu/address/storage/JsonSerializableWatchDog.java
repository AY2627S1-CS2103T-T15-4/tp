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
import seedu.address.model.ReadOnlyWatchDog;
import seedu.address.model.WatchDog;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * An Immutable WatchDog that is serializable to JSON format.
 */
@JsonRootName(value = "watchdog")
@JsonIgnoreProperties("persons")
class JsonSerializableWatchDog {

    public static final String MESSAGE_DUPLICATE_CUSTOMER = "Customers list contains duplicate customer(s).";
    public static final String MESSAGE_DUPLICATE_DOG = "Dog list contains duplicate dog(s).";

    private final List<JsonAdaptedCustomer> customers = new ArrayList<>();
    private final List<JsonAdaptedDog> dogs = new ArrayList<>();
    private final List<JsonAdaptedActivity> activities = new ArrayList<>();
    private final List<JsonAdaptedEnrolment> enrolments = new ArrayList<>();
    private final Long nextActivityId;
    private final Long nextDogId;
    private final Long nextCustomerId;
    /** Constructs a serializable WatchDog from JSON fields. */
    @JsonCreator
    public JsonSerializableWatchDog(@JsonProperty("customers") List<JsonAdaptedCustomer> customers,
            @JsonProperty("dogs") List<JsonAdaptedDog> dogs,
            @JsonProperty("nextDogId")
            @JsonDeserialize(using = NextDogIdDeserializer.class) Long nextDogId,
            @JsonProperty("nextCustomerId") Long nextCustomerId,
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
    public JsonSerializableWatchDog(List<JsonAdaptedCustomer> customers, List<JsonAdaptedDog> dogs,
            Long nextDogId, Long nextCustomerId, List<JsonAdaptedActivity> activities, Long nextActivityId) {
        this(customers, dogs, nextDogId, nextCustomerId, activities, null, nextActivityId);
    }

    /**
     * Converts a given {@code ReadOnlyWatchDog} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableWatchDog}.
     */
    public JsonSerializableWatchDog(ReadOnlyWatchDog source) {
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
     * Converts this WatchDog into the model's {@code WatchDog} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public WatchDog toModelType() throws IllegalValueException {
        WatchDog watchDog = new WatchDog();
        for (JsonAdaptedCustomer jsonAdaptedCustomer : customers) {
            if (jsonAdaptedCustomer == null) {
                throw new IllegalValueException("Customers list must not contain null entries.");
            }
            Customer customer = jsonAdaptedCustomer.toModelType();
            if (watchDog.hasCustomer(customer)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CUSTOMER);
            }
            watchDog.addCustomer(customer);
        }

        for (JsonAdaptedDog jsonAdaptedDog : dogs) {
            if (jsonAdaptedDog == null) {
                throw new IllegalValueException("Dog list must not contain null entries.");
            }
            Dog dog = jsonAdaptedDog.toModelType();
            if (watchDog.hasDog(dog)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_DOG);
            }
            try {
                watchDog.addDogFromStorage(dog);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }

        if (nextDogId != null) {
            try {
                watchDog.setNextDogId(nextDogId);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        if (nextCustomerId != null && nextCustomerId >= 1) {
            try {
                // Older versions could persist MAX_VALUE even after assigning it.
                watchDog.setNextCustomerId(Math.max(nextCustomerId, watchDog.getNextCustomerId()));
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }

        // @@author Kenneth-Chia
        for (JsonAdaptedActivity adapted : activities) {
            if (adapted == null) {
                throw new IllegalValueException("Activities list must not contain null entries.");
            }
            Activity activity = adapted.toModelType();
            try {
                // @@author
                watchDog.addActivityFromStorage(activity);
            // @@author Kenneth-Chia
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        if (nextActivityId != null) {
            try {
                // @@author
                watchDog.setNextActivityId(nextActivityId);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        for (JsonAdaptedEnrolment adapted : enrolments) {
            if (adapted == null) {
                throw new IllegalValueException("Enrolments list must not contain null entries.");
            }
            Activity activity = adapted.getActivityId() == null ? null : watchDog.getActivityList().stream()
                    .filter(candidate -> candidate.getId() == adapted.getActivityId())
                    .findFirst()
                    .orElse(null);
            if (activity == null) {
                throw new IllegalValueException("Invalid or duplicate enrolment.");
            }
            Enrolment enrolment = adapted.toModelType(activity.getStartDate());
            try {
                watchDog.addEnrolmentFromStorage(enrolment);
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage(), e);
            }
        }
        return watchDog;
    }

}
