package seedu.address.logic.commands;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/** Command formats, examples and specifications for help in the result display. */
public final class CommandHelp {
    private static final String DOG_FIELDS = "Names and breeds must be nonblank letters, numbers or spaces. "
            + "Size is small, medium, large or giant (case-insensitive).";
    private static final String CUSTOMER_FIELDS = "Phone and backup contact must contain exactly eight digits; "
            + "spaces between digits are removed. Names allow letters, numbers, spaces, hyphens and apostrophes; "
            + "punctuation must be followed by a letter or number. Email must be valid. "
            + "Primary phone and email must be unique across customers (email ignores case); "
            + "backup contacts may be shared.";
    private static final String IDS = "Use persistent record IDs, including hidden records; "
            + "Dog and customer IDs range from 0 to 2147483647; activity IDs from 1 to 2147483647.";
    private static final String ACTIVITY_FIELDS = "Dates use YYYY-MM-DD and times use 24-hour HH:mm. "
            + "End must be later than start on the same day. Recurrence is lowercase once, daily or weekly.";

    private static final Map<String, String> HELP = Map.ofEntries(
            entry(AddDogCommand.COMMAND_WORD, "adddog n/NAME b/BREED s/SIZE",
                    "adddog n/Cookie b/Chihuahua s/Small",
                    "Adds a dog with a unique persistent ID. All three fields are required exactly once. "
                            + DOG_FIELDS),
            entry(AddCustomerCommand.COMMAND_WORD, "addcustomer n/NAME p/PHONE e/EMAIL bc/BACKUP_CONTACT",
                    "addcustomer n/Lily Koh p/91234567 e/lily@gmail.com bc/98765432",
                    "Adds a customer with a unique persistent ID. All four fields are required exactly once. "
                            + CUSTOMER_FIELDS),
            entry(EditDogCommand.COMMAND_WORD, "editdog DOG_ID [n/NAME] [b/BREED] [s/SIZE]",
                    "editdog 1 s/Large",
                    "Requires an existing dog and at least one field. Omitted fields and ownership are preserved. "
                            + IDS + " " + DOG_FIELDS),
            entry(EditCustomerCommand.COMMAND_WORD,
                    "editcustomer CUSTOMER_ID [n/NAME] [p/PHONE] [e/EMAIL] [bc/BACKUP_CONTACT]",
                    "editcustomer 1 p/91234567 e/johndoe@example.com",
                    "Requires an existing customer and at least one field. Omitted fields are preserved. "
                            + IDS + " " + CUSTOMER_FIELDS),
            entry("delete", "delete dog DOG_ID OR delete customer CUSTOMER_ID", "delete dog 0",
                    "Specify the category and an existing persistent ID. " + IDS),
            entry(DeleteDogCommand.COMMAND_WORD, "delete dog DOG_ID", "delete dog 0",
                    "Deletes the dog and all its activity enrolments, including past dates. "
                            + "Other dogs, customers and activity schedules remain. " + IDS),
            entry(DeleteCustomerCommand.COMMAND_WORD, "delete customer CUSTOMER_ID", "delete customer 21",
                    "Deletes an existing customer and unlinks all their dogs without deleting the dogs. " + IDS),
            entry(LinkCommand.COMMAND_WORD, "link c/CUSTOMER_ID d/DOG_ID [DOG_ID]...", "link c/1 d/2 3",
                    "Put exactly one c/ field first, then one d/ field with distinct IDs separated by whitespace. "
                            + "Prefixes are lowercase; no whitespace after the slash. "
                            + "All records must exist and every dog must be unowned. "
                            + "Any invalid record rejects the entire batch. Filters stay active. " + IDS),
            entry(UnlinkCommand.COMMAND_WORD, "unlink d/DOG_ID [DOG_ID]... OR unlink c/CUSTOMER_ID",
                    "unlink d/2 3",
                    "Use one d/ field with whitespace-separated IDs. Do not mix dog and customer prefixes. "
                            + "Dog IDs must be distinct, existing and owned; "
                            + "the customer form requires an existing customer with linked dogs. "
                            + "The whole batch fails if any record is invalid. No records are deleted. " + IDS),
            entry(SearchDogCommand.COMMAND_WORD, "searchd [n/NAME] [b/BREED] [s/SIZE] [i/DOG_ID]",
                    "searchd b/Golden Retriever s/Medium",
                    "Supply at least one field; all supplied fields must match. Name and breed use "
                            + "case-insensitive substring matching; size is small, medium, large or giant. "
                            + "ID must identify an existing dog. Also filters owners and enrolled activities. "
                            + "Filters remain active as records change."),
            entry(SearchCustomerCommand.COMMAND_WORD,
                    "searchc [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_ID ...] [bc/BACKUP_CONTACT] [i/CUSTOMER_ID]",
                    "searchc n/Anna d/12 34",
                    "Supply at least one field; all supplied fields must match. Name uses case-insensitive "
                            + "substring matching with repeated spaces treated as one. Phone and backup contact "
                            + "are exactly eight digits and match exactly; email matches exactly ignoring case. "
                            + "IDs must exist; customers must own all specified dogs. "
                            + "Also filters owned dogs and their enrolled activities; filters remain active."),
            entry(FilterUnlinkedCommand.COMMAND_WORD, "filterun dog OR filterun customer", "filterun dog",
                    "Category must be lowercase. Shows unowned dogs or customers without dogs, "
                            + "and clears the other panel. The filter remains active as records change."),
            entry(AddActivityCommand.COMMAND_WORD,
                    "addactivity n/NAME date/YYYY-MM-DD start/HH:mm end/HH:mm r/RECURRENCE",
                    "addactivity n/Grooming date/2026-11-20 start/14:00 end/15:00 r/once",
                    "All five fields are required exactly once in any order. Name must be nonblank. "
                            + "Creates a schedule with no enrolled dogs; past dates and overlapping empty "
                            + "schedules are allowed. " + ACTIVITY_FIELDS),
            entry(EditActivityCommand.COMMAND_WORD,
                    "editactivity ACTIVITY_ID [n/NAME] [date/YYYY-MM-DD] [start/HH:mm] [end/HH:mm] [r/RECURRENCE]",
                    "editactivity 1 n/Training",
                    "Requires an existing activity with no enrolments and at least one field to edit. "
                            + "Omitted fields are preserved. " + ACTIVITY_FIELDS),
            entry(DeleteActivityCommand.COMMAND_WORD, "deleteactivity ACTIVITY_ID", "deleteactivity 21",
                    "Deletes an existing activity's entire series and all its enrolments. "
                            + "Dogs and other activities remain; deleted activity IDs are not reused. " + IDS),
            entry(SearchActivityCommand.COMMAND_WORD,
                    "searcha [n/NAME] [d/YYYY-MM-DD] [t/HH:mm] [r/RECURRENCE] [i/ACTIVITY_ID]",
                    "searcha n/Dog run t/16:00",
                    "Supply at least one field; all supplied fields must match. Name matching ignores case "
                            + "and repeated spaces. Date includes recurring occurrences; time matches inclusively "
                            + "between start and end. Recurrence is Once, Weekly or Daily, ignoring case. "
                            + "Also shows enrolled dogs and their owners; a supplied date limits enrolments "
                            + "to that occurrence. Filters remain active as records change."),
            entry(EnrolDogCommand.COMMAND_WORD, "enrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID1,DOG_ID2",
                    "enrol a/1 date/2026-11-20 d/2,3",
                    "Activity and distinct dog IDs must exist. Date must be today or a future occurrence "
                            + "of the activity. Duplicate enrolments and overlapping activities for the same "
                            + "dog are rejected. " + IDS),
            entry(UnenrolDogCommand.COMMAND_WORD, "unenrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID1,DOG_ID2",
                    "unenrol a/1 date/2026-11-20 d/2,3",
                    "Removes only the specified dated enrolments. Activity and distinct dog IDs must exist "
                            + "and the dogs must be enrolled on that date. " + IDS),
            entry(TodayCommand.COMMAND_WORD, "today", "today",
                    "Accepts no parameters. Shows activities occurring today, dogs enrolled in today's "
                            + "occurrences and their owners. Replaces all three displays; empty panels mean "
                            + "no matching records. Includes recurring activities."),
            entry(ResetDisplayCommand.COMMAND_WORD, "reset", "reset",
                    "Accepts no parameters. Shows all customers and dogs, and schedules with an occurrence "
                            + "today or later using the computer's local date. Hides past one-time activities. "
                            + "Changes the view only and can be undone."),
            entry(SummaryCommand.COMMAND_WORD, "summary", "summary",
                    "Accepts no parameters. Shows overall dogs and customers, total and upcoming activity "
                            + "schedules, and today's activities and distinct enrolled dogs. Uses all stored "
                            + "records, including hidden and unlinked records. Each schedule counts once; "
                            + "upcoming includes recurring schedules with future occurrences. Today's counts "
                            + "include empty activities and sessions that already ended. Each dog ID counts "
                            + "once across today's enrolments; enrolment does not indicate attendance. "
                            + "Uses the computer's local date. Preserves records, filters and undo/redo history."),
            entry(ClearCommand.COMMAND_WORD, "clear", "clear (then enter Y to confirm or N to cancel)",
                    "Asks for confirmation before deleting all customers, dogs, activities and enrolments. "
                            + "Y/N is case-insensitive. Enter N before running another command. "
                            + "Confirmation clears undo/redo history and cannot be undone; "
                            + "cancellation preserves it."),
            entry(UndoCommand.COMMAND_WORD, "undo", "undo",
                    "Accepts no parameters. Restores records and view from before the most recent undoable "
                            + "command. Only one command can be undone. Help and summary preserve history; "
                            + "confirmed clear discards it. History is not retained across restarts."),
            entry(RedoCommand.COMMAND_WORD, "redo", "redo",
                    "Accepts no parameters. Restores the records and view from just before the last undo. "
                            + "A successful undoable command discards redo history. Help and summary preserve it. "
                            + "A redone command can be undone again."),
            entry(HelpCommand.COMMAND_WORD, "help [COMMAND]", "help summary",
                    "Without a command, opens the help window. With a command name, shows its format, example "
                            + "and specifications here. Use lowercase names, including multiword names such as "
                            + "delete dog. Do not include the target command's arguments. "
                            + "Preserves undo/redo history."),
            entry(ExitCommand.COMMAND_WORD, "exit", "exit", "Exits the application."));

    private CommandHelp() {
    }

    /** Returns help for an exact, normalized command name. */
    public static Optional<String> getHelp(String topic) {
        return Optional.ofNullable(HELP.get(topic));
    }

    /** Returns supported topics in alphabetical order for error feedback. */
    public static String getTopics() {
        return String.join(", ", new TreeSet<>(HELP.keySet()));
    }

    private static Map.Entry<String, String> entry(String topic, String format, String example, String specifications) {
        return Map.entry(topic, "Format: " + format + "\nExample: " + example
                + "\nSpecifications: " + specifications);
    }
}
