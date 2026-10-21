package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.dog.Dog;

/** Shows one dog, its owner, and that dog's current and future activities. */
public class SelectDogCommand extends Command {

    public static final String COMMAND_WORD = "select";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " dog <id>: Shows the dog, its owner, and its current and future activities.\n"
            + "Example: " + COMMAND_WORD + " dog 21";
    public static final String MESSAGE_SUCCESS = "Selected dog %1$d: %2$s";
    public static final String MESSAGE_INVALID_ID = "No such dog. Input a valid dog id";

    private final int dogId;

    /** Creates a command that selects the dog with the specified ID. */
    public SelectDogCommand(int dogId) {
        this.dogId = dogId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Dog selectedDog = model.getDogList().stream()
                .filter(dog -> dog.getId() == dogId)
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_INVALID_ID));

        LocalDate today = LocalDate.now();
        Set<Integer> activityIds = model.getAddressBook().getEnrolmentList().stream()
                .filter(enrolment -> enrolment.getDogId() == selectedDog.getId())
                .map(Enrolment::getActivityId)
                .collect(Collectors.toSet());

        model.updateFilteredDogList(dog -> dog.getId() == selectedDog.getId());
        model.updateFilteredCustomerList(customer -> selectedDog.getOwnerId()
                .filter(ownerId -> ownerId == customer.getId()).isPresent());
        model.updateFilteredActivityList(activity -> activityIds.contains(activity.getId())
                && !activity.isCompleted()
                && activity.nextOccurrenceOnOrAfter(today).isPresent());
        return new CommandResult(String.format(MESSAGE_SUCCESS, selectedDog.getId(), selectedDog.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SelectDogCommand otherCommand && dogId == otherCommand.dogId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("dogId", dogId).toString();
    }
}
