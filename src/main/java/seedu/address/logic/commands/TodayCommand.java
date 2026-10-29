package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import seedu.address.model.Model;

/** Displays today's activities together with their enrolled dogs and owners. */
public class TodayCommand extends Command {
    public static final String COMMAND_WORD = "today";
    public static final String MESSAGE_SUCCESS = "Successfully displayed today's activities, "
            + "as well as the dogs and customers involved for the day";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        new SearchActivityCommand(null, LocalDate.now(), null).execute(model);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TodayCommand;
    }
}
