---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# WatchDog User Guide

WatchDog is a **desktop application for managing dog daycare customers and dogs, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, WatchDog can help you manage daycare records faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T15-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for WatchDog.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar WatchDog.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On a fresh launch without saved data,
   WatchDog includes two sample customers. The screenshot shows additional records and bookings added for illustration.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some commands to try:

   * `reset`: Shows all customers and dogs, plus activities occurring today or later.
   * `summary`: Shows overall counts and today's activity figures.
   * `today`: Shows today's activities, enrolled dogs, and their owners.
   * `exit`: Exits the app.

1. Follow the [worked example](#worked-example), then use the [Features](#features) section for command details.

--------------------------------------------------------------------------------------------------------------------

## Worked example

This example follows one customer and dog from registration to an activity booking. WatchDog assigns IDs when records
are added; use the IDs shown in your own command feedback in place of `CUSTOMER_ID`, `DOG_ID`, and `ACTIVITY_ID`.

1. Add a customer: `addcustomer n/Priya Menon p/97654321 e/priya@example.com bc/88776655`.
2. Add a dog: `adddog n/Cookie b/Chihuahua s/Small`.
3. Link them: `link c/CUSTOMER_ID d/DOG_ID`.
4. Create a one-time activity: `addactivity n/Grooming date/2030-06-10 start/14:00 end/15:00 r/once`.
5. Book the dog: `enrol a/ACTIVITY_ID date/2030-06-10 d/DOG_ID`.

If 10 June 2030 has passed when you try this example, replace that date in both commands with a future date.
You can use `searchc`, `searchd`, or
`searcha` to find the records afterward, and `reset` to return to the overview.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Record IDs are persistent positive integers. Activity IDs range from `1` to `2147483647`.

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `adddog n/NAME b/BREED s/SIZE`, replace `NAME` with a value such as `Cookie`.

* Items in square brackets are optional.<br>
  For example, `editdog ID [n/NAME] [b/BREED] [s/SIZE]` allows you to specify only the fields to change.

* An optional item followed by `...` can appear zero or more times.<br>
  For example, `link c/7 d/42 51` links multiple dogs to one customer.

* Field parameters can be in any order unless stated otherwise. `link` requires `c/` before `d/`.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* `reset` and `summary` reject extra parameters. `help` accepts an optional command name.
  Some commands without parameters, such as `exit` and `clear`, ignore extra input.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

The original AB3 person commands `add`, `edit`, `delete INDEX` and `find` are no longer supported.
Use the customer and dog commands described below.

### Customers

#### Adding a customer: `addcustomer`

Adds a customer and assigns it a unique persistent ID.

Format: `addcustomer n/NAME p/PHONE_NUMBER e/EMAIL bc/BACKUP_CONTACT_NUMBER`

Example:

* `addcustomer n/Priya Menon p/97654321 e/priya@example.com bc/88776655`

Customer names may contain only English letters (`A-Z`, `a-z`), digits (`0-9`), spaces, hyphens (`-`),
and straight apostrophes (`'`). Accented letters (such as `é`), Chinese characters, and curly apostrophes
(`’`) are not supported. Names must start with an English letter or digit. A hyphen or apostrophe must
be followed by an English letter or digit; repeated punctuation is not allowed. For example, `Jean-Luc`
and `O'Neil` are valid names.

Email addresses must use the `local-part@domain` format. Apostrophes are allowed in the local part
before `@`.

The contact number and backup contact number must each contain exactly eight digits. You may enter
spaces between digits; the spaces are removed when the number is stored. All four fields are required.
Each customer's primary phone number and email address must be unique across all customers, including
those hidden by filters. Email uniqueness ignores letter case. Backup contact numbers may be shared,
including with another customer's primary phone number.
If fields are missing, WatchDog lists them and shows the command format. If a value is invalid, it
reports the first invalid field and its constraints.

#### Editing a customer: `editcustomer`

Edits one or more details of an existing customer using its persistent ID.

Format: `editcustomer CUSTOMER_ID [n/NAME] [p/PHONE] [e/EMAIL] [bc/BACKUP_CONTACT]`

Example: `editcustomer 1 p/91234567 e/johndoe@example.com`

* `CUSTOMER_ID` must identify an existing customer.
* Any supplied field replaces its existing value.
* The resulting primary phone number and email address must not belong to another customer. You may
  keep your existing phone number and email address. Backup contact numbers need not be unique.
* Supplied names, phone numbers, backup contacts, and email addresses follow the same validation rules
  as `addcustomer`.

#### Searching customers: `searchc`

Searches customers by one or more fields. All supplied fields must match.

Format: `searchc [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_ID ...] [bc/BACKUP_CONTACT] [i/CUSTOMER_ID]`

Example: `searchc n/Anna d/12 34`

* Name matching is case-insensitive, uses a substring search, and treats repeated spaces as one space.
* Phone and backup contact numbers require exactly eight digits and use exact matching.
* Email matching is case-insensitive and exact.
* Dog IDs must be positive and exist. A customer must own all specified dog IDs to match.
* Customer ID must be a positive ID belonging to an existing customer; it matches that customer exactly.

* The dog panel is also filtered to show only dogs owned by the matching customers.
* Dog IDs and customer ID appear in the success message only when supplied; dog IDs are displayed in ascending numeric order.
* Only lowercase `n/`, `p/`, `e/`, `d/`, `bc/`, and `i/` prefixes are accepted.
* Invalid field values display `Error in searching customers:` followed by the applicable error message. Invalid or nonexistent dog and customer IDs have specific messages.
  Dog IDs display `Please enter a valid dog index`; customer IDs display `Please enter a valid customer index`.
  Zero, negative, nonnumeric, and out-of-range IDs are invalid. If any ID is invalid, the entire search is rejected
  and the previous display is preserved.
* Unsupported field prefixes are rejected as invalid command formats.
* If no customers match, the customer panel is empty.
* The filter remains active while customers or their linked dogs are added, edited, deleted, linked, or unlinked.
  The activity panel shows only activities with enrolments belonging to the matching customers. Each activity
  includes an `Enrolments` section containing only their dogs and those dogs' stored enrolment dates.

#### Deleting a customer: `deletecustomer`

Deletes an existing customer using its persistent ID and removes their dogs' ownership links.
The dogs and their activity bookings remain, and the dogs can be linked to another customer.
The customer can be deleted even when hidden by a search or filter.

Format: `deletecustomer ID`

Example:

* `deletecustomer 21`
* `deletecustomer   21`

Leading and trailing whitespace and multiple spaces between the command name and ID are accepted.

### Dogs

#### Adding a dog: `adddog`

Adds a dog and assigns it a unique persistent ID.

Format: `adddog n/NAME b/BREED s/SIZE`

Example:

* `adddog n/Cookie b/Chihuahua s/Small`

Valid sizes are `small`, `medium`, `large`, and `giant`. Size input is case-insensitive.
Dog names may contain letters, numbers, spaces, hyphens, and apostrophes, and must not be blank. Breeds may contain
letters, numbers, spaces, and hyphens, and must not be blank. Leading and trailing
spaces in field values are ignored.

#### Editing a dog: `editdog`

Edits one or more details of an existing dog using its persistent ID.

Format: `editdog DOG_ID [n/NAME] [b/BREED] [s/SIZE]`

Example: `editdog 1 s/Large`

* `DOG_ID` must identify an existing dog.
* At least one dog field must be provided.
* Any omitted field keeps its existing value.

#### Searching dogs: `searchd`

Searches dogs by one or more fields. All supplied fields must match.

Format: `searchd [n/NAME] [b/BREED] [s/SIZE] [i/ID]`

Example: `searchd b/Golden Retriever s/Medium`

* Name and breed matching are case-insensitive substring searches.
* Size must be `small`, `medium`, `large`, or `giant`, ignoring letter case.
* ID matching is exact; dog IDs are positive integers and must belong to a dog in the database.
* A successful command displays `Successfully searched dogs by` followed by only the supplied fields.
* Invalid input displays `Error in searching dogs:` followed by the applicable error message. Invalid or nonexistent
  dog IDs display `Please enter a valid dog index`.
* Invalid sizes display `Invalid dog size. Please input small/medium/large/giant only.`
* The customer panel is also filtered to customers linked to the matching dogs.
* The activity panel shows activities in which a matching dog is enrolled.
  For each displayed activity, an `Enrolments` section lists every matching dog as
  `[DOG_ID]. [DOG_NAME]: DATE, DATE, ...`. Dates include past, present, and future enrolments
  and are sorted chronologically. The activity's normal date, time, and recurrence fields remain unchanged.
* Unowned matching dogs remain visible, but the customer panel is empty.
* The filter remains active while dogs or customers are added, edited, deleted, linked, or unlinked.
  Matching activities refresh when enrolments change.

#### Deleting a dog: `deletedog`

Deletes a dog using its persistent dog ID and removes all of that dog's activity enrolments, including past
occurrences. Other dogs' enrolments and the activity schedules remain unchanged.

Format: `deletedog ID`

Examples:

* `deletedog 1`
* `deletedog   1`

Multiple spaces between the command components are accepted.

Upon success, WatchDog displays:

`Successfully deleted dog <id>: <Name>; Breed: <breed>; Size: <Size>`

If the ID does not exist, WatchDog displays:

`Error in deleting dog:`<br>`Please enter a valid dog index`

### Dog ownership

#### Customer and dog cards

The customer and dog panels are separate scrollable areas. Customer cards show contact details and the IDs of
their dogs. Dog cards show the persistent dog ID and name, followed by breed, size, and customer ID when linked.
Newly added records appear automatically. Customer and dog IDs belong to separate categories and may overlap.

Hover over a contact-detail or dog-detail row to see its full value when the displayed text is shortened.
Right-click a customer or dog card to copy an individual field or all details in a labelled, multiline format.
An unlinked dog's customer-ID action is disabled. The app briefly confirms a successful copy and reports when
the operating system clipboard rejects it.

#### Linking dogs to a customer: `link`

Assigns one or more existing, unowned dogs to an existing customer. Use this to record who owns each dog;
one command can link several dogs to the same customer.

Format: `link c/CUSTOMER_ID d/DOG_ID [DOG_ID]...`

Example: `link c/1 d/2 3` links dogs 2 and 3 to customer 1.

* Use persistent record IDs, including records hidden by a search.
* Put exactly one `c/` field first, followed by one `d/` field with space-separated dog IDs.
  Prefixes are lowercase, and there must be no space between a prefix and its ID.
* Each customer can own multiple dogs; each dog can have only one owner. Unlinked records are allowed.
* Editing a dog's name, breed, or size preserves its owner. Use `unlink` to remove ownership.
* Customer and dog IDs must be positive integers; dog IDs must not repeat. Unknown fields, missing IDs, zero or negative IDs,
  and non-integer IDs are rejected.
* If the customer or any dog does not exist, or any dog already has an owner (even the same customer),
  the entire command fails and no records change. Linking does not reassign dogs.
* A successful command reports the linked IDs. Cards show the updated ownership, including dogs hidden by a search;
  existing filters stay active. Use `reset` to see all records.
* Links are saved automatically. Deleting a customer unlinks their dogs, including hidden dogs, without deleting them.
  Deleting a dog does not delete its customer.

#### Unlinking dogs from a customer: `unlink`

Removes ownership links from selected dogs or from all dogs belonging to one customer. The dogs and
customers remain in WatchDog, so you can subsequently link the dogs to another customer.

Formats: `unlink d/DOG_ID [DOG_ID]...` or `unlink c/CUSTOMER_ID`

Examples:

* `unlink d/1 2 3` removes ownership from dogs 1, 2 and 3, even if they have different owners.
* `unlink c/1` removes ownership from every dog belonging to customer 1, including hidden dogs.

* Customer and dog IDs are positive integers. Use lowercase prefixes and whitespace between dog IDs.
  Do not put whitespace after the slash. Repeated dog IDs (including `d/2 02`) are rejected.

* Do not mix customer and dog prefixes. The customer form accepts exactly one customer ID.
* For the dog form, every dog must exist and have an owner. If any dog is missing or unowned,
  the whole batch fails without changing anything.
* For the customer form, the customer must exist and have at least one linked dog; otherwise an error is shown.
* Unlinking never deletes records or changes their other details. Cards show the updated ownership;
  existing search filters stay active. Use `link` to assign the dogs to another customer.
* Changes are saved automatically.

### Activities and bookings

#### Adding an activity: `addactivity`

Creates an activity schedule with zero dogs and reports its persistent ID. The schedule is saved
across restarts. Activities with identical names or times are allowed and receive different IDs.

Format: `addactivity n/NAME date/YYYY-MM-DD start/HH:mm end/HH:mm r/RECURRENCE`

All five fields are required exactly once, in any order. Prefixes and recurrence values are lowercase.
Use a nonblank name, a valid calendar date, and 24-hour times from `00:00` to `23:59`.
The end must be later than the start on the same day; overnight schedules are unsupported.
Past start dates are permitted. Leading and trailing spaces in values are ignored.
Space-separated alphabetic tokens ending in `/` are reserved for prefixes, including within names;
unknown prefixes and extra text before the fields are rejected without creating an activity.

* `r/once`: occurs only on the supplied date.
* `r/daily`: occurs every day from that date, with no end date.
* `r/weekly`: occurs on that date's weekday every week, with no end date.

Examples:

* `addactivity n/Grooming date/2030-06-10 start/14:00 end/15:00 r/once`
* `addactivity n/Morning walk date/2030-06-10 start/09:00 end/09:30 r/daily`
* `addactivity n/Training date/2030-06-10 start/10:00 end/11:00 r/weekly`

Invalid dates, times, recurrence values, missing or repeated fields produce an error.
Creation does not book any dogs, so overlapping empty schedules are allowed. Dog booking commands
and activity cards are separate features; this contribution reports creation in the command feedback.

#### Reading activity cards

An activity card describes a reusable schedule, not a single booking. `Schedule starts: YYYY-MM-DD`
is the schedule's first occurrence; daily and weekly schedules can continue after that date.

In the general activity panel, `Dogs with upcoming bookings: ...` lists the distinct dog IDs that have
dated bookings for that activity today or later. Today remains included even after the activity's end time.
IDs are sorted numerically and a dog booked on several upcoming dates appears once. The label shows `None`
when there are no upcoming bookings, without hiding the activity.

The separate Today panel uses `Dogs enrolled on YYYY-MM-DD: ...` and includes only bookings on that exact
local date. It reads all stored enrolments independently of dog and customer search filters. These labels do
not change the activity's start date, recurrence, or other schedule details.

The date is captured from the computer's local timezone whenever the panels refresh. There is no midnight
timer; after midnight, the next successful command or activity/enrolment refresh updates the labels.

#### Editing an activity: `editactivity`

Edits an activity with the specified persistent ID. An activity can be edited only when it has no enrolments.
If dogs are enrolled, the command leaves the activity unchanged and suggests creating a new activity instead.
Supply at least one field to change; omitted fields retain their current values. For a recurring activity,
the changes apply to the entire schedule.

Format: `editactivity ACTIVITY_ID [n/NAME] [date/YYYY-MM-DD] [start/HH:mm] [end/HH:mm] [r/once|daily|weekly]`

Example: `editactivity 21 n/Morning walk start/09:00 end/10:00` changes activity 21's name and times,
provided it exists and has no enrolments. Its other details remain unchanged.

#### Searching activities: `searcha`

Searches activities by name, date, time, recurrence, and/or activity ID. At least one field must be provided, and all supplied
fields must match.

Format: `searcha [n/NAME] [d/YYYY-MM-DD] [t/HH:mm] [r/RECURRENCE] [i/ACTIVITY_ID]`

Example: `searcha n/Dog run t/16:00`

* Name matching is case-insensitive (`run` and `RUN` are equivalent) and treats repeated internal spaces as one space.
* Date matching includes activities that occur on the specified date, including daily and weekly recurrences.
* Time matching is inclusive: an activity matches when the specified time is between its start and end times.
* Recurrence matching accepts `Once`, `Weekly`, or `Daily`, and is case-insensitive.
* Activity ID must be a positive ID in the database and matches exactly. It combines with other fields using AND.
  Invalid or nonexistent IDs display `Error in searching activities:` followed by
  `Please enter a valid activity index`. Zero, negative, nonnumeric, and out-of-range IDs are invalid.
  Invalid IDs reject the entire search and preserve the previous display. Repeated field prefixes are rejected.
* Dates must use `YYYY-MM-DD`, and times must use 24-hour `HH:mm` format.
* Only lowercase `n/`, `d/`, `t/`, `r/`, and `i/` field prefixes are accepted.
* A successful command displays `Successfully searched activities by` followed by the supplied values, in the form
  `name: NAME`, `date: YYYY-MM-DD`, `time: HH:mm`, `recurrence: Once|Weekly|Daily`, and `activity id: ACTIVITY_ID`. Unused fields are omitted.
  If there are no matches, the activity panel is empty.
* The dog panel shows dogs enrolled in the matching activities; when a date is supplied, only enrolments on that
  occurrence date are included. The customer panel shows owners of those dogs.
* Invalid date, time, or recurrence input displays `Error in searching activities:` followed by the applicable error message.
 * The filter remains active while activities are added or their state, enrolments, or details are changed.

#### Deleting an activity: `deleteactivity`

Deletes the activity with the specified persistent ID. For a recurring activity, this deletes the
entire series. All dogs enrolled in that activity are unenrolled; the dogs themselves and their
enrolments in other activities remain. Deleted activity IDs are not reused.

Format: `deleteactivity ACTIVITY_ID`

Example: `deleteactivity 21`

The ID must be a positive integer (1 to 2147483647) belonging to an existing activity. Invalid or missing IDs
leave the activity and enrolments unchanged.

#### Enrolling dogs in activities: `enrol` and `unenrol`

`enrol` books one or more existing dogs into a specific dated occurrence of an existing activity.
Create the dogs and activity first, then supply their persistent IDs and the date to book.
For a daily or weekly activity, each command books only the supplied date; it does not automatically
book the dogs into every future occurrence. Dogs can be enrolled whether or not they have an owner.

`unenrol` cancels the selected dogs' bookings for that activity on the supplied date. Bookings for
other dates and activities remain. The activity schedule and the dog and customer records also remain,
so you can use `enrol` again to book another occurrence. Enrolments represent planned participation,
not attendance or check-in.

Formats: `enrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]` and
`unenrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]`.

Examples:

* `enrol a/1 date/2030-06-10 d/1` books dog 1 in activity 1's occurrence on 10 June 2030.
* `unenrol a/1 date/2030-06-10 d/1` removes that dated booking.

These examples use the Grooming activity and Cookie from the worked example, assuming they received IDs 1 and 1.
Replace those IDs with the ones shown in your command feedback. If 10 June 2030 has passed, choose a future
occurrence date in both the activity and enrolment commands. The dog must not already be booked or have an
overlapping booking. Run `unenrol` only after the corresponding `enrol` succeeds.

The `a/`, `date/`, and `d/` fields may appear in any order and must each appear exactly once. Dog IDs are separated
by one or more spaces, must be positive integers, and must not repeat; commas and `d/0` are rejected. Activity IDs
must also be positive integers and are supplied separately using `a/`.

* The date must be an occurrence of the selected activity: its start date for a one-time activity,
  or a date on or after its start date that follows its daily or weekly recurrence.
* `enrol` rejects a dog already booked into that occurrence or booked into another activity with an
  overlapping time on the same date. Adjacent activities, where one ends exactly when another starts,
  are allowed.
* `unenrol` requires every selected dog to have a booking for that exact activity and date.
* Both commands apply to the whole batch: if any dog or booking is invalid, none of the requested
  bookings are added or removed. Existing search filters remain active, and affected cards refresh.

Invalid input reports the specific field or formatting problem, followed by the complete command format and an
example. `enrol` rejects past dates and states that the date must be today or later. `unenrol` permits past dates so
that a historical enrolment can be corrected, provided the activity occurred and the dog was enrolled on that date.

### Viewing and filtering

#### Showing today's activities: `today`

Shows activities occurring today, the dogs enrolled in today's occurrences, and their owners.

Format: `today`

Example: `today`

* The command is case-sensitive and accepts no additional fields.
* Recurring activities are included when they occur today.
* Dogs are shown only when they are enrolled in today's occurrence of an activity.
* The command replaces the current activity, dog, and customer displays.
* If there are no matching records, the relevant panels are empty.
* Extra input produces `Error in showing today:` followed by
  `Info is in the wrong format. Input in the correct format`.

#### Viewing daycare statistics: `summary`

Shows six statistics using all stored records, including records hidden by search or filters.
The command preserves your records and filters.

Format: `summary` (no parameters; surrounding whitespace is accepted)

Example: `summary` displays the statistics below using your stored records and the current local date.

Example output:

```text
Overall: 24 dogs | 18 customers
Activity schedules: 10 total | 7 from today onwards
Today (YYYY-MM-DD): 5 activities | 12 distinct dogs enrolled
```

* Overall counts include dogs without owners and customers without dogs. Records with the same name
  but different IDs count separately.
* Total activity schedules includes past one-time activities. Each recurring schedule counts once.
* From today onwards counts schedules with an occurrence today or later, including recurring
  schedules that started earlier. It excludes past one-time activities.
* Today's activities counts schedules occurring today, including activities with no enrolled dogs.
  All activity counts include empty activities and sessions that ended earlier today.
* Today's distinct dogs enrolled counts each dog ID once across all activities on that date.
  Yesterday's and tomorrow's enrolments are excluded. Enrolment does not indicate attendance.

The date shown is read once when the command runs, using the computer's local date.
WatchDog attempts to save after running `summary`. If saving fails, the statistics are still displayed
and the failure is recorded in the application log.

#### Filtering unlinked records: `filterun`

Finds incomplete ownership records by showing either dogs without owners or customers without dogs.
This changes the displayed records and keeps the filter active as ownership changes.

Format: `filterun dog` or `filterun customer`

Examples:

* `filterun dog` finds dogs without an owner.
* `filterun customer` finds customers without dogs.

* `filterun dog` displays dogs that are not linked to any customer and clears the customer panel.
  The activity panel shows activities those dogs are enrolled in, including past enrolments. Each activity's
  `Enrolments` section shows only unowned dogs and their stored enrolment dates.
* `filterun customer` displays customers that do not own any dogs and clears the dog and activity panels.
* The category must be lowercase. Multiple spaces between `filterun` and the category are accepted.
* The filter remains active while records are added, edited, deleted, linked, or unlinked.
  Matching activities also refresh when enrolments change.

#### Resetting the display: `reset`

Shows all stored customers and dogs, plus activities with an occurrence today or later according to
the computer's local date. One-time activities before today are hidden. A daily or weekly activity
remains visible while it has a future occurrence, even if its first date is in the past. Activity
cards continue to show the schedule's first date. This command changes the view only; stored records
are not changed.

Format: `reset`

Example: `reset` restores the overview after a search or filter.

### Help and data management

#### Viewing help: `help`

Opens a help window containing the link to this User Guide. Use `help COMMAND` to display a specific
command's format, example, and specifications directly in the result section.

![help message](images/helpMessage.png)

Format: `help`

To view a command's format, example and specifications in the result section (where success and error
messages appear), use `help COMMAND`. This does not open the help window or execute the named command.

Examples:

* `help` opens the help window.
* `help summary`
* `help adddog`
* `help deletedog`
* `help reset`

Use the lowercase command name without its arguments. Surrounding whitespace and multiple spaces
between `help` and the command name are accepted. An unknown command name or
extra arguments (for example, `help summary today`) display an error and the supported command names.
Plain `help` still opens the original help window.

Both forms preserve records, filters, and undo/redo history. For example, after adding a dog and
running `help adddog`, `undo` undoes the addition. Running help after `undo` also preserves `redo`.

#### Undoing and redoing a command: `undo`, `redo`

`undo` restores the stored records and displayed view from before the most recent undoable command.
`redo` restores the records and view from just before that undo. Both commands attempt to save the
restored records automatically.

Formats: `undo` or `redo`

For example, after `adddog n/Cookie b/Chihuahua s/Small`, enter `undo` to remove Cookie,
then `redo` to bring Cookie back. You can undo the redone command again.

Only one command can be undone at a time. A later successful undoable command replaces that history
and makes `redo` unavailable. View-changing commands such as search, filter, selection, and
`reset` are undoable too. `help`, `summary`, and `exit` preserve both undo and redo history;
parse errors and commands rejected before execution also leave history unchanged. The history is
cleared after confirming `clear` with `Y` and is not retained when WatchDog restarts. Surrounding whitespace is accepted,
but neither command accepts arguments.
If no command is available, WatchDog displays `There is no command to undo.` or
`There is no command to redo.`

A save error can occur after an undoable command, `undo`, `redo`, or `clear` has changed memory.
The change remains visible in the current session, with the corresponding undo or redo operation
available where applicable, but the data file may not contain that change.

#### Clearing all entries: `clear`

Clears all customers, dogs, activities, and enrolments from WatchDog after confirmation.

Format: `clear`

The app warns that this action cannot be undone and asks for confirmation (Y/N).
Enter `Y` to clear all entries or `N` to cancel in the command box. Both answers are
case-insensitive and accept surrounding whitespace. Any other input asks for Y/N again;
enter `N` before running another command. No data or history changes until you confirm.

Examples:

* Enter `clear`, then enter `y` to permanently delete all entries.
* Enter `clear`, then enter `n` to cancel and keep all entries and undo/redo history.

Clearing also discards the undo and redo history, so `clear` cannot be undone. This remains true
if the command changes memory but its save fails.

Clearing preserves the current search filters and display selection. Newly added records appear only
if they match the active view. For example, after `searchc n/Alice` followed by `clear` and `Y`, adding
a customer named Bob succeeds but does not display Bob in the customer panel. Use `reset` to show all
customers and dogs, plus activities occurring today or later.

#### Saving the data

WatchDog automatically saves data after every command. You do not need to save manually.

#### Editing the data file

WatchDog data is saved automatically as `data/watchdog.json` inside the folder from which you launch WatchDog (the home folder in Quick Start). Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, WatchDog starts with no saved records at the next run. The invalid file remains on disk until you run a command (WatchDog saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause WatchDog to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

#### Exiting the program: `exit`

Closes WatchDog. Record changes are saved automatically after commands; window preferences are saved
when the application shuts down. Undo and redo history is not retained after exiting.

Format: `exit`

Example: `exit` closes WatchDog.

#### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous WatchDog home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add customer** | `addcustomer n/NAME p/PHONE_NUMBER e/EMAIL bc/BACKUP_CONTACT_NUMBER`<br>e.g., `addcustomer n/Priya Menon p/97654321 e/priya@example.com bc/88776655`
**Edit customer** | `editcustomer CUSTOMER_ID [n/NAME] [p/PHONE] [e/EMAIL] [bc/BACKUP_CONTACT]`<br>e.g., `editcustomer 1 p/91234567`
**Search customers** | `searchc [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_ID ...] [bc/BACKUP_CONTACT] [i/CUSTOMER_ID]`<br>e.g., `searchc n/Anna i/21`
**Delete customer** | `deletecustomer ID`<br>e.g., `deletecustomer 21`
**Add dog** | `adddog n/NAME b/BREED s/SIZE`<br>e.g., `adddog n/Cookie b/Chihuahua s/Small`
**Edit dog** | `editdog DOG_ID [n/NAME] [b/BREED] [s/SIZE]`<br>e.g., `editdog 1 s/Large`
**Search dogs** | `searchd [n/NAME] [b/BREED] [s/SIZE] [i/DOG_ID]`<br>e.g., `searchd n/bud s/small`
**Delete dog** | `deletedog ID`<br>e.g., `deletedog 1`
**Link dogs** | `link c/CUSTOMER_ID d/DOG_ID [DOG_ID]...`<br>e.g., `link c/1 d/2 3`
**Unlink dogs** | `unlink d/DOG_ID [DOG_ID]...` or `unlink c/CUSTOMER_ID`<br>e.g., `unlink d/2 3` or `unlink c/1`
**Add activity** | `addactivity n/NAME date/YYYY-MM-DD start/HH:mm end/HH:mm r/once`<br>Use `r/daily` or `r/weekly` for recurrence.
**Edit activity** | `editactivity ACTIVITY_ID [n/NAME] [date/YYYY-MM-DD] [start/HH:mm] [end/HH:mm] [r/RECURRENCE]`<br>e.g., `editactivity 21 n/Morning walk start/09:00 end/10:00`
**Search activities** | `searcha [n/NAME] [d/YYYY-MM-DD] [t/HH:mm] [r/RECURRENCE] [i/ACTIVITY_ID]`<br>e.g., `searcha n/Dog run t/16:00`
**Delete activity** | `deleteactivity ACTIVITY_ID`<br>e.g., `deleteactivity 21`
**Enrol dogs** | `enrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]`<br>Date must be today or a future occurrence.
**Unenrol dogs** | `unenrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]`<br>Removes only the specified dated enrolments.
**Today** | `today`<br>Shows today's activities, enrolled dogs, and owners.
**Daycare summary** | `summary`<br>Shows overall and today's statistics.
**Filter unlinked** | `filterun dog` or `filterun customer`
**Reset display** | `reset`
**Help**   | `help` opens the help window; `help COMMAND` shows command details in the result section.<br>e.g., `help summary`, `help adddog`, `help deletedog`
**Undo** | `undo`
**Redo** | `redo`
**Clear** | `clear`, then `Y` to confirm or `N` to cancel. Cannot be undone.
**Exit** | `exit`
