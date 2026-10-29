---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# WatchDog Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* WatchDog adapts the architecture, utilities and documentation of [AddressBook Level-3](https://github.com/se-edu/addressbook-level3).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Persistent identities

Customers, dogs and activities use persistent IDs as their identities. These IDs are assigned by the
address book and are stored with the records, so they remain stable across application restarts and
are not affected by filtering, list positions or deletion of other records. This allows commands and
relationships to refer to a record reliably even when the visible list changes.

### Customer–dog ownership

`LinkCommandParser` accepts `c/CUSTOMER_ID d/DOG_ID [DOG_ID]...`: one customer field followed by
one dog field containing distinct, whitespace-separated IDs. Repeated `d/` prefixes are rejected.
`LinkCommand` delegates the batch to `Model.linkDogs`; `ModelManager` forwards to `AddressBook`.
The address book looks up persistent IDs in the complete lists and validates every endpoint and ownership
constraint before publishing one replacement dog list. A rejected batch throws `LinkException` without
changing records or filters; the command translates it to `CommandException` for user feedback.
After successful link or unlink publication, an internal assertion checks that every requested dog ID
still exists and has the intended owner, or no owner for unlink. These checks do not mutate records or
replace the ordinary validation of command input.

Each immutable `Dog` stores an optional owner customer ID. This is the single source of truth;
customers do not store a duplicate dog-ID list. A dog may be unlinked or have one owner, while a customer
may own any number of dogs. Linking an already-owned dog is rejected, including links to the same owner.
Dog equality includes ownership, while identity remains the persistent dog ID.

`UnlinkCommandParser` accepts either `c/CUSTOMER_ID` or `d/DOG_ID [DOG_ID]...` with distinct,
whitespace-separated dog IDs in one field; repeated dog prefixes and mixed prefixes
are rejected. `UnlinkCommand` delegates to `Model.unlinkCustomerDogs` or `Model.unlinkDogs` and translates
`UnlinkException` into command feedback. `AddressBook` validates every selected dog exists and has an owner
before publishing one replacement list of unowned copies. Dogs may belong to different customers.
The customer form validates the customer, selects all their dogs from the complete list, and uses the
same atomic batch operation. Missing customers and customers with no linked dogs are rejected.
Unlinking preserves records and filters, including a customer's last dog, and uses the existing nullable
`ownerId` storage field without a schema change.

`AddressBook.removeCustomer` clears ownership of that customer's dogs, regardless of filters,
preserving their persistent IDs and details. Unowned dog cards show `Owner ID: None`.
Removing a dog leaves its customer intact. Customer edits must preserve persistent IDs to keep links valid.
Observable list updates refresh dog cards, which display owner IDs without changing search filters.
Customer cards display owned dog IDs below backup contact. `RecordListPanel` observes the complete dog list
as well as visible records, so these IDs remain accurate when dog cards are filtered out.

`JsonAdaptedDog` persists `ownerId`. A missing or null field means unlinked, preserving compatibility
with older files. Negative owner IDs and references to missing customers are rejected on load;
customers are restored before dogs. Copies of the address book retain ownership through immutable dogs.
The shared model boundaries also reject dangling owner IDs: `setDog` validates the replacement,
`setCustomers` checks that existing owners remain present, and `resetData` validates ownership against
snapshots of the incoming dogs and customers before changing records. Customer deletion uses its explicit
unlinking path instead of removing a referenced customer through `setCustomers`.
`EditDogCommand` preserves the current owner ID when constructing a replacement dog, so editing details
does not unlink the dog. Ownership changes remain explicit through linking, unlinking, or customer deletion.
Deleting a dog also removes its enrolment records in the same address-book operation, including past
occurrences; other dogs' enrolments and activity schedules are preserved.

### Dog–activity enrolment

Dog participation in an activity occurrence is represented by a separate `Enrolment` record containing
an activity ID, a dog ID, and an occurrence date. This separates the reusable activity schedule from
the dogs attending it while distinguishing occurrences of recurring activities.
It also allows dogs to be enrolled or unenrolled independently without modifying the activity's
schedule or storing duplicate dog objects inside `Activity`.

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete dog 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `RecordListPanel`,
`ActivityListPanel`, `TodayActivityListPanel`, and `StatusBarFooter`. `TodayActivityListPanel` reuses the
activity list and filters it to schedules occurring today. All of these, including `MainWindow`, inherit
from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

`RecordListPanel` observes the filtered customer and dog lists, plus the complete dog list exposed by `Logic`.
It displays customers followed by dogs in a single list, using `CustomerCard` and `DogCard`.
List changes rebuild the card factories; JavaFX creates card nodes as cells need them.
Cards use persistent IDs, not row numbers. The complete dog list supplies ownership details independently of filters.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Customer` and `Dog` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete dog 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete dog 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteDogCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteDogCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteDogCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a dog).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddDogCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddDogCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddDogCommandParser` and `DeleteDogCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores customers and dogs in `UniqueCustomerList` and `UniqueDogList`, with persistent IDs and dog ownership links.
* exposes separate unmodifiable observable customer and dog lists for search and category filtering. The UI observes these lists to update cards when records or filters change.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)



### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Removal of the legacy person feature

WatchDog uses customer and dog records. The original AB3 person records, tags, person-only
commands (`add`, `edit`, `delete INDEX` and `find`), person storage adapters and unused list panels
have been removed. Shared components such as `AddressBook`, preferences, parsing utilities,
help, `clear` and JSON storage remain in use.

Existing JSON files may contain a legacy `persons` field. It is ignored on loading and omitted
on the next save; its records are not converted to customers. Customer/dog records, ownership
links and both next-ID counters are preserved. A file containing only legacy persons loads
as an empty WatchDog book. No startup sample person records are created.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

### Adding dogs

`AddDogCommandParser` validates the dog details supplied by the user and creates an
`AddDogCommand`. The command delegates to `Model`, which forwards the request through
`ModelManager` to `AddressBook`.

The address book assigns the dog a persistent ID when it is added. The ID is allocated from a
monotonically increasing counter rather than from the dog's position in the list, so deleting a dog
does not change the identities of the remaining dogs. The counter and dog IDs are persisted so that
new dogs continue to receive unique IDs after the application restarts.

The following diagram shows the add-dog command flow. The important part of this flow is that the
persistent dog ID is assigned by the address book only
after the command reaches the model layer.

<puml src="diagrams/AddDogSequenceDiagram.puml" alt="Adding a dog through the application layers" />

### Editing dogs

`EditDogCommandParser` parses the persistent dog ID and the fields to be edited. The command uses
the ID to locate the dog in the complete dog list rather than relying on a filtered-list position.
`AddressBook` validates the replacement details and publishes the updated dog while preserving its
persistent ID and current owner relationship.

The following diagram shows the edit-dog command flow. The command locates the dog using its
persistent ID, so editing remains independent of the current
filtered-list position. The replacement dog retains both its ID and owner relationship.

<puml src="diagrams/EditDogSequenceDiagram.puml" alt="Editing a dog through the application layers" />

### Retired list commands

The former list dog and list customer commands are no longer routed by AddressBookParser.
Use reset to restore all customers and dogs and upcoming activities, or searchc/searchd
to narrow the display. Retired list-dog source and tests are preserved under unused/@xandergoh/;
list-customer source, parser, and tests are under unused/@its-shoutime/. These archives are
excluded from Gradle source and test sets and retain authorship annotations.

### Retired select commands

The former select dog and select customer implementations are retained under
unused/@its-shoutime/ for authorship reference only. They are not routed by the active
AddressBookParser and are not supported by the final product.

### Undo and redo

`AddressBookParser` accepts `undo` and `redo` only without arguments. `LogicManager` handles these
commands before ordinary command execution; `UndoCommand.execute` and `RedoCommand.execute` alone
cannot restore history. `LogicManager` keeps one undo snapshot and one redo snapshot, each paired
with the original command text for feedback. A successful undoable command replaces the undo
snapshot with the state captured before its execution and clears the redo snapshot. View-changing
commands are undoable. `help`, `summary`, and `exit` preserve both slots, as do parse errors and
commands rejected during execution. `clear` first puts `LogicManager` into a pending confirmation
state and returns a warning without saving or changing history. The next input must be `Y` or `N`
(case-insensitive, with surrounding whitespace accepted). Invalid answers leave confirmation pending;
`N` cancels without changing data or history. `Y` executes `ClearCommand`, discards both snapshots,
and cannot itself be undone. Confirmation is consumed even if saving the cleared data fails.

`Model.createState()` copies the `AddressBook`, including its records and all three next-ID counters, and captures
the active customer, dog and activity predicates and the selected record IDs. `Model.restoreState()`
resets the existing address book and reapplies the saved display state. Reusing the existing model
and observable lists lets UI listeners continue to see updates after a restoration. It refreshes
predicates twice: the first pass installs source filters, and the second re-evaluates dependent filters.
This handles both dog filters whose customer predicate reads filtered dogs and customer filters whose
dog predicate reads filtered customers.

On `undo`, `LogicManager` captures the current state for redo, restores the undo snapshot, moves
the history slots, then saves the address book. `redo` symmetrically captures the current state for
undo, restores the redo snapshot, moves the slots, and saves. An ordinary command updates history
immediately after successful execution and before saving. This ordering matches the existing policy
of retaining in-memory changes when a save fails: the corresponding inverse remains available,
and `clear` still invalidates history. The disk may contain an older state, or a partially written
state if an actual write had started. Missing history produces a `CommandException`; save failures
are reported as command errors. History is held only in memory and is unavailable after a restart.

`LogicManagerTest` and `LogicManagerHistoryTest` cover the undo/redo cycle, neutral commands,
invalidated history, `clear`, failed commands, dependent filters, and save failures.
`ModelManagerTest` checks restoration of data, ID counters, relationships, filters, and selection, while
`AddressBookParserTest` checks argument validation.

### Daycare summary

`AddressBookParser` accepts `summary` with blank arguments only and constructs `SummaryCommand`.
At execution, the command captures `LocalDate.now(clock)` once and uses that date for every count
and the feedback date. The default clock uses the system's local zone; tests inject a clock.
Counts use the full model lists, so filters and dog/customer selection remain unchanged.

Each stored activity schedule contributes once to the total. A schedule contributes to the
from-today-onwards count when `nextOccurrenceOnOrAfter(today)` is present, and to today's activity
count when `occursOn(today)` is true. Empty activities and sessions that ended earlier today are
included; recurring series are never expanded into future dates.

`Model.getDogsEnrolledOn(LocalDate)` returns an unmodifiable snapshot. `ModelManager` collects dog
IDs from enrolments on that date into a set, then resolves them against the full dog list in its
existing order. This counts a dog enrolled in multiple activities once while keeping same-name
dogs with different IDs distinct. Null dates are rejected with `NullPointerException`.
The query takes time proportional to the number of enrolments plus dogs and stores no cached count.

Feedback follows the existing `CommandResult` and `ResultDisplay` path. No records, filters or
selection are mutated, and no storage schema changes are needed. `LogicManager` still attempts
to save after successful execution, preserving its existing save-error handling. Summary neither
creates an undo entry nor discards redo history.

### Activity creation and storage

The following partial class diagram shows the activity schedules and their integration with the model.
An activity represents a reusable schedule; its occurrence dates are calculated from its recurrence rule.

<puml src="diagrams/ActivityClassDiagram.puml" alt="Activity schedule classes and model integration" />

`AddressBookParser` routes `addactivity` to `AddActivityCommandParser`. The parser requires
`n/`, `date/`, `start/`, `end/`, and `r/` exactly once, rejects unknown alphabetic prefixes,
and validates strict calendar dates, minute-resolution 24-hour times and lowercase recurrence.
The immutable command validates a details snapshot with an unused ID of zero. Parsing never allocates
an ID. `execute` calls `Model.addActivity(ActivityName, LocalDate, LocalTime, LocalTime, Recurrence)`
and reports the identified schedule returned by the model. No dog/customer filters change.

`ModelManager` delegates to `AddressBook.addActivity`. The address book validates the complete
schedule before assigning the next ID and publishing it through `getActivityList()`, an unmodifiable
observable view of all schedules. Creation allows identical details and overlapping empty schedules.
The first allocated ID is one; the monotonically increasing counter is stored as a long so that
`Integer.MAX_VALUE + 1` can represent exhaustion without wrapping. ID exhaustion rejects creation.
Copy/reset and equality include activities and the counter. Existing customer/dog APIs are unchanged.

`JsonAdaptedActivity` stores the six schedule fields; dates/times use ISO strings and recurrence uses
its enum name. `JsonSerializableAddressBook` stores `activities` and `nextActivityId`, validates
identities and the counter, and rejects invalid schedules or null entries. Older files with no
activity fields load an empty collection. A missing counter is derived from the largest stored ID;
an explicit counter must exceed every stored ID and cannot exceed the exhaustion sentinel.
Activity IDs and the counter must be integer JSON numbers; fractions, numeric strings and
out-of-range numbers are rejected before conversion. Reset restores identity counters before replacing
activities and enrolments. An internal activity list batches activity notifications until enrolments
are restored, so listeners of either list see the restored schedules, enrolments and counters.
`LogicManager` uses its existing save path after successful command execution.

The following sequence diagram shows successful execution of
`addactivity n/Training date/2026-10-20 start/10:00 end/11:00 r/weekly`.
The command uses the `Model` interface, implemented by `ModelManager`; saving uses the `Storage`
interface, implemented by `StorageManager`. Concrete lifelines are shown to make the delegation clear.

<puml src="diagrams/AddActivitySequenceDiagram.puml" alt="Parsing, executing and saving an addactivity command" />

### Deleting an activity

`AddressBookParser` routes `deleteactivity` to `DeleteActivityCommandParser`, which accepts one
positive persistent ID. The command finds the matching activity in `Model.getActivityList()` and
passes it to `Model.deleteActivity`. `ModelManager` delegates to `AddressBook.removeActivity`, which
checks that the activity is still stored, removes its enrolments, and then removes the activity.
The next activity ID does not change, so deleted IDs are not reused. After the command returns its
success message, `LogicManager` saves the updated address book.

<puml src="diagrams/DeleteActivitySequenceDiagram.puml" alt="Parsing, executing and saving a deleteactivity command" />

### Editing an activity

`AddressBookParser` routes `editactivity` to `EditActivityCommandParser`. The parser requires a
persistent activity ID and at least one replacement field. `EditActivityCommand` finds the stored
activity by ID and rejects the edit if any enrolment references that activity, suggesting that the
user create a new activity instead. Fields omitted from a permitted edit retain their stored values.
For an activity with no enrolments, the command builds an immutable replacement with the same ID and
calls `Model.setActivity`.

`ModelManager` delegates the replacement to `AddressBook`, which checks that the activity is still
stored and that the edited schedule does not overlap another activity attended by the same dog.
Enrolments are never removed by editing. `LogicManager` then saves the address book.

<puml src="diagrams/EditActivitySequenceDiagram.puml" alt="Parsing, executing and saving an editactivity command" />

### Dog search

`AddressBookParser` routes `searchd` to `SearchDogCommandParser`. The parser accepts the lowercase fields
`n/`, `b/`, `s/`, and `i/`, rejects duplicate or unknown fields, normalises whitespace, and validates sizes
and nonnegative IDs. `DogSearchPredicate` performs case-insensitive substring matching for names and breeds,
case-insensitive exact matching for sizes, and exact matching for IDs.

`SearchDogCommand` rejects IDs that are not present in the database before changing filtered lists. It applies
the dog predicate, derives owners from the current matching dogs, and filters activities to schedules with at
least one enrolled dog matching the same predicate. Thus unowned matching dogs remain visible while the customer
list is empty, and activities with no matching enrolments are excluded. The predicates use current model data,
so dog, customer, activity, and enrolment changes refresh the corresponding panels while the filter remains active.
Each `Enrolment` stores its occurrence date. `searcha` with a date therefore shows only dogs enrolled on that
specific occurrence, and shows their owners only when those dogs are visible. `searchd` shows a schedule when a
matching dog is enrolled on a valid occurrence of that schedule.
For `searchd`, the activity panel additionally receives a derived enrolment summary. It groups matching stored
enrolments by dog ID, removes duplicate dates, sorts both dogs and dates deterministically, and displays the
dates in ISO format. The summary includes past, present, and future dates; recurrence is never expanded into
unstored dates. Other activity-card contexts retain the normal card format.
Successful executions use the `Successfully searched dogs by` prefix and report only supplied fields. Invalid
IDs and sizes use the `Error in searching dogs:` prefix with their specific validation messages.

<puml src="diagrams/SearchDogSequenceDiagram.puml" alt="Filtering dogs and related owners and activities" />

### Customer search

`AddressBookParser` routes `searchc` to `SearchCustomerCommandParser`. The parser accepts the optional
lowercase fields `n/`, `p/`, `e/`, `d/`, `bc/`, and `i/`. Names are normalised for case-insensitive substring
matching, while contact numbers and email addresses use exact matching. Multiple dog IDs are parsed from
the `d/` field and are retained as a set. The optional customer ID is parsed as a positive integer and
matched exactly against existing customer IDs.

`SearchCustomerCommand` validates that the requested customer ID and every requested dog ID exist, then applies
the customer ID criterion together with `CustomerSearchPredicate` to the filtered customer list. The predicate
derives ownership from each dog's stored `ownerId`; a customer
matches a multi-dog filter only when they own every requested dog. After customer filtering, the command derives
the matching customer IDs and applies a second predicate to the dog list, showing only dogs owned by those
customers. Dog IDs in the success message are sorted numerically, and customer ID is reported only when supplied.
It also filters the activity panel to activities with enrolments belonging to the matching customers and supplies
the same per-dog enrolment summary used by `searchd`. An activity with no matching enrolment is omitted, so the
activity panel cannot show unrelated cards or cards with misleading empty enrolment details.
Invalid or nonexistent dog and customer IDs produce specific errors under the `Error in searching customers:` prefix.
The parser stops field values at any field-like prefix, including unsupported prefixes, so inputs such as
`searchc n/Anna x/value` are rejected rather than treating `x/value` as part of the name. `ModelManager`
reapplies active customer and dog predicates after customer edits, dog edits, and relationship changes so
both filtered views remain consistent.

<puml src="diagrams/SearchCustomerSequenceDiagram.puml" alt="Searching customers and their owned dogs" />
### Activity search

`AddressBookParser` routes `searcha` to `SearchActivityCommandParser`. The parser accepts at least one
of the lowercase fields `n/`, `d/`, `t/`, and `r/`, rejects duplicate or unknown fields, and validates dates
and times using strict `YYYY-MM-DD` and `HH:mm` formats. Recurrence is restricted to `Once`, `Weekly`,
or `Daily`. Names are trimmed, internal whitespace is normalised, and matching is case-insensitive.

`SearchActivityCommand` (invoked by `searcha`) applies an `ActivitySearchPredicate` to the model's filtered activity list.
Name matching uses a case-insensitive substring, date matching delegates to `Activity.occursOn(date)`
so recurring activities are handled correctly, and time matching includes both endpoints. Multiple
recurrence matching compares the activity's stored recurrence. Multiple fields are combined with AND semantics.
 The activity panel observes this filtered list, while the
underlying stored schedules remain unchanged. The success message reports only the supplied fields, in the
order name, time, date, and recurrence.
The activity predicate is reapplied after activity additions and activity state or enrolment changes, so the
activity display remains filtered without requiring the user to repeat the command. Successful executions return
`Successfully searched activities by` with only the supplied fields; invalid date, time, or recurrence values
use the `Error in searching activities:` prefix. Invalid command shapes retain the existing invalid-format response
with the command usage.
The command also derives the dog panel from enrolments in matching activities and the customer panel from those
dogs' owners. These predicates inspect current model data, so relationship changes refresh all three displays.
When the filter contains a date, dog visibility additionally requires an enrolment on that exact occurrence date;
the owners panel is derived from those dogs. When no date is supplied, enrolments from any occurrence may match.
Gradle test execution enables Java assertions so internal model invariants are checked during verification.

<puml src="diagrams/SearchActivitySequenceDiagram.puml" alt="Searching activities and related dogs and owners" />
### Today command

`AddressBookParser` routes the exact lowercase command `today` to `TodayCommandParser`, which rejects
any additional arguments. `TodayCommand` delegates the filtering behavior to `SearchActivityCommand`
with `LocalDate.now()` as the date criterion. This reuses the existing recurrence-aware activity
matching and derives dogs from enrolments on today's exact occurrence, followed by their owners.
The command replaces the active activity, dog, and customer displays and returns
`Successfully displayed today's activities, as well as the dogs and customers involved for the day`.

### Unlinked filtering

`AddressBookParser` routes `filterun` to `FilterUnlinkedCommandParser`, which accepts only the lowercase
categories `dog` and `customer`, with arbitrary surrounding whitespace. `FilterUnlinkedCommand` filters dogs
whose `ownerId` is absent or customers with no owned dogs, and clears the related panel. Its predicates are
stored by `ModelManager`, so the results refresh after later record or relationship changes.

<puml src="diagrams/FilterUnlinkedSequenceDiagram.puml" alt="Filtering unlinked dogs or customers" />

### Activity enrolment

Dogs are linked to activity occurrences through separate `Enrolment` records. Each record stores an
activity ID, a dog ID, and an occurrence date, allowing an activity to have multiple dogs without
storing dog objects directly inside the reusable activity schedule.

Users can enrol or unenrol one or more dogs using:

```text
enrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID1,DOG_ID2
unenrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID1,DOG_ID2
```

The parser converts the activity ID, occurrence date, and comma-separated dog IDs into a command.
The command delegates through `Model` and `ModelManager` to `AddressBook`, which verifies that the
activity and dogs exist, the date is a valid occurrence and is today or later, and the requested
enrolments are valid before updating the enrolment list. A dog cannot be enrolled in another activity
with an overlapping time on the same date; adjacent activities are allowed. Batch operations are
atomic. Successful mutations are logged at `FINE` and guarded by postcondition assertions. Enrolment
changes are persisted with the address book.

<puml src="diagrams/EnrolDogSequenceDiagram.puml" alt="Enrolling dogs into an activity" />

Unenrolment follows the same parser and model delegation path. After validating the activity, date,
dogs, and exact dated enrolments, the address book removes only the matching activity/dog/date records
instead of changing the activity schedule itself. Past dates may be unenrolled for correction. Successful
removal is logged and checked with a postcondition assertion.

<puml src="diagrams/UnenrolDogSequenceDiagram.puml" alt="Unenrolling dogs from an activity" />

`ActivityEnrolmentTest` covers recurrence validation, atomic batches, overlap boundaries and error details;
the enrol and unenrol command/parser tests cover command construction and malformed or impossible dates.
`EnrolmentStorageTest` covers persisted and legacy records. `DeleteDogCommandTest` checks that deleting a dog
removes both past and future enrolments without affecting other dogs. `ListPanelsTest` and `MainWindowTest`
cover the today-only activity filter and its UI integration.

The sequence diagrams focus on interactions between application layers. Low-level list operations
and exception propagation are omitted where they do not affect the overall design.

### Design considerations

Use `Model.getActivityList()` for observing activities and `Activity.getId()` for identity,
independently of displayed positions. Use `Activity.occursOn(date)` for dated schedule queries.
An activity's start date is its first occurrence. Enrolments are stored separately from activity schedules
and are keyed by activity ID, dog ID, and occurrence date. Legacy enrolment records without a date are
migrated to their activity's first date when loaded.
`MainWindow` displays all activities alongside a today-only panel and a date-specific heading. The panel
reacts to activity-list changes through the shared observable list.
`addActivityFromStorage` and `setNextActivityId` are restoration APIs, not command entry points.

### Defensive programming, logging, and assertions

Command arguments and model invariants are validated explicitly and produce domain or parser exceptions;
Java assertions are reserved for internal postconditions, never for user-input validation. Ownership
updates assert that all requested dog endpoints exist with the expected owner after publication. Enrolment,
unenrolment, and dog deletion assert that the expected dated records are present or absent after a successful
mutation. The Gradle `run` and `test` tasks enable assertions so these checks are active during development
and automated testing.

`LogicManager` records each command. `AddressBook` logs successful enrolment changes and dog deletion at
`FINE`, including the number of enrolment records removed. An invalid enrolment restored from storage is
logged at `WARNING` with its IDs, date, and validation reason before the load error is propagated. Expected
user validation failures are returned through command feedback rather than logged as warnings, avoiding
misleading error logs for routine input mistakes.

`AddActivityCommandParserTest`, `AddActivityCommandTest`, `ActivityIntegrationTest` and
`ActivityStorageTest` cover parser failures, unchanged state, observable access, identity boundaries,
all recurrence modes, old files and command-to-disk round trips using temporary files.

### Search commands

The active category searches are `searcha`, `searchc`, and `searchd`. Each parser accepts its documented field prefixes and constructs the corresponding field-based command. The commands replace the relevant filtered observable list and keep related dogs, customers, and activities consistent.

`SearchDogCommandParser` accepts `n/`, `b/`, `s/`, and `i/`; `SearchCustomerCommandParser` accepts `n/`, `p/`, `e/`, `d/`, `bc/`, and `i/`; and `SearchActivityCommandParser` accepts `n/`, `d/`, `t/`, and `r/`. Unsupported prefixes, duplicate fields, invalid IDs, invalid sizes, and malformed values are rejected before execution.

The global search command and its category compatibility routes have been retired. Use the field-based searchc, searchd, and searcha commands (i/ for persistent IDs). Retired global search code, tests, and orphaned helpers are preserved under unused/@Kenneth-Chia/. The list commands and reset remain available.

Search command tests cover parser validation, field combinations, related-record updates, persistent IDs, and unchanged stored records.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* Is a dog day care centre manager
* Has a need to manage multiple dogs and customers
* Has a need to keep track of each dog’s information and schedule
* Has a need to keep track of every dog’s owner and their information
* Can type fast
* Prefers typing to mouse interactions
* Is reasonably comfortable using CLI apps

**Value proposition**: WatchDog helps dog day care centre managers to manage and keep track of all their clients’ dogs, each of the dog’s needs and client information in a centralised application, faster than with a typical mouse-driven GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a/an                                                | I want to...                                                               | So that I can...                                                  |
|----------|--------------------------------------------------------|----------------------------------------------------------------------------|-------------------------------------------------------------------|
| `* * *`  | Dog day-care manager setting up the system             | Create a customer profile                                                  | I can store each customer’s information in one place              |
| `* * *`  | Dog day-care manager                                   | Add customer contact information                                           | I can contact customers when necessary                            |
| `* * *`  | Dog day-care manager                                   | Add a dog’s information to an information card                             | I can keep track of each dog’s needs                              |
| `* * *`  | Dog day-care manager                                   | Link customer profiles to their dogs                                       | I can identify which dogs belong to each customer                 |
| `* * *`  | Busy dog day-care manager                              | Search for a specific customer                                             | I can quickly find their information                              |
| `* * *`  | Dog day-care manager                                   | Edit customer details                                                      | I can keep customer information up to date                        |
| `* * *`  | Dog day-care manager                                   | Edit dog details                                                           | I can record new or changed information                           |
| `* * *`  | Dog day-care manager                                   | Create and edit activity records                                           | I can update activities when plans change                         |
| `* * *`  | Forgetful dog day-care manager                         | Maintain a checklist for each dog                                          | I can ensure that all required tasks are completed                |
| `* * *`  | Dog day-care manager managing multiple dogs            | Schedule activities for customers and their dogs                           | I can keep track of upcoming activities                           |
| `* * *`  | Dog day-care manager preparing for the day             | View all activities scheduled for a particular day                         | I know what needs to be prepared each day                         |
| `* * *`  | Dog day-care manager                                   | Mark scheduled activities as completed                                     | I can keep track of which tasks have been carried out             |
| `* * *`  | Dog day-care manager                                   | View a list of dogs currently present                                      | I can monitor the dogs at the day-care centre                     |
| `* * *`  | Dog day-care manager                                   | Record when a dog checks in and checks out                                 | I can accurately track which dogs are currently present           |
| `* * *`  | Dog day-care manager responsible for dog safety        | Record multiple emergency contacts for each dog                            | I can contact someone if the owner is unavailable                 |
| `* * *`  | Dog day-care manager entering information              | View clear error messages when invalid information or commands are entered | I can correct mistakes without crashing the program               |
| `* *`    | Dog day-care manager                                   | View all dogs belonging to a specific customer                             | I can see every dog owned by that customer                        |
| `* *`    | Busy dog day-care manager                              | Filter dogs by characteristics such as size, breed, or temperament         | I can quickly find dogs with specific characteristics             |
| `* *`    | Busy dog day-care manager                              | Filter dogs based on their required activities                             | I can identify which dogs need a particular activity              |
| `* *`    | Dog day-care manager                                   | Add new categories or filters                                              | I can customise searches as the business changes                  |
| `* *`    | Forgetful dog day-care manager                         | View the total number of dogs enrolled for each day                        | I can determine whether there is enough capacity for new bookings |
| `* *`    | Dog day-care manager managing customer records         | Reassign a dog to a different customer profile                             | I can accurately record changes in ownership                      |
| `* *`    | Unorganised dog day-care manager                       | Remove a dog from the database                                             | I can keep the database free of irrelevant information            |
| `* *`    | Unorganised dog day-care manager                       | Remove a customer from the database                                        | I can maintain an organised list of active customers              |
| `* *`    | Dog day-care manager                                   | Add and view recently updated pictures of dogs                             | I can easily identify each dog                                    |
| `* *`    | Busy dog day-care manager                              | Sort the daily dog or activity list                                        | I can allocate staff more efficiently                             |
| `* *`    | Dog day-care manager managing capacity                 | Set the maximum daily capacity for activities                              | I can prevent too many activities from being registered           |
| `* *`    | Dog day-care manager managing schedules                | Define the operating hours of the day-care centre                          | I can ensure activities are scheduled within working hours        |
| `* *`    | Forgetful dog day-care manager                         | Automate recurring attendance schedules                                    | I do not have to enter the same schedule repeatedly               |
| `* *`    | Dog day-care manager                                   | Create a backup file of the database                                       | I can recover the data if it is lost                              |
| `* *`    | Dog day-care manager                                   | Record notes or incidents for each dog                                     | I can keep track of important events during a dog’s stay          |
| `* *`    | Dog day-care manager                                   | View a dog’s activity and attendance history                               | I can review the dog’s previous visits and care                   |
| `* *`    | Busy dog day-care manager                              | View upcoming bookings in a calendar or list                               | I can plan staffing and resources in advance                      |
| `*`      | New dog day-care manager                               | Access a quick-start guide                                                 | I can learn to use WatchDog quickly and confidently               |
| `*`      | Careless dog day-care manager                          | Recover recently deleted data                                              | I can undo accidental deletions                                   |
| `*`      | Dog day-care manager working with colleagues           | Share the database with colleagues                                         | My colleagues can also access and manage dog information          |
| `*`      | Dog day-care manager concerned about dietary safety    | Use AI to review a dog’s diet based on its recorded information            | I can identify potential dietary concerns                         |
| `*`      | Dog day-care manager concerned about medication safety | Use AI to review a dog’s medication based on its recorded information      | I can identify potential medication concerns                      |

### Use cases


(For all use cases below, the **System** is the 'Watchdog' and the **Actor** is the user, unless specified otherwise)

**Use case: UC01 - Add Dog**

**MSS**

1.  User enters the command and the dog details to add a dog.
2.  System validates the command and dog details.
3.  System adds the dog and displays a success message.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.

* 2a. System detects invalid dog details.

    * 2a1. System requests for correct details.

      Use case resumes from step 1.



**Use case: UC02 - View Dog**

**MSS**

1.  User requests to restore the display using `reset`.
2.  System shows all dogs and customers, plus upcoming activities.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.



**Use case: UC03 - Edit Dog**

**MSS**

1.  User requests to <ins>list dogs (UC03)</ins>.
2.  User enters the command, dog details and the dog id for the dog to be edited.
3.  System validates the command, dog details and dog id.
4.  System edits the dog and displays a success message.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.

* 2a. System detects invalid dog details.

    * 2a1. System requests the correct details.

      Use case resumes from step 2.

* 2b. System cannot find the specified dog id.

    * 2b1. System requests for an existing dog id.

      Use case resumes from step 2.



**Use case: UC04 - Delete Dog**

**MSS**

1.  User requests to <ins>list dogs (UC03)</ins>.
2.  User enters the command and the dog id for the dog to be deleted.
3.  System validates the command and dog id.
4.  System deletes the dog and displays a success message.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.

* 2a. System cannot find the specified dog id.

    * 2a1. System requests for an existing dog id.

      Use case resumes from step 2.



**Use case: UC05 - Add Customer**

**MSS**

1.  User enters the command and the customer details to add a customer.
2.  System validates the command and customer details, including the uniqueness of the primary phone number and email address.
3.  System adds the customer and displays a success message.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.

* 2a. System detects invalid customer details.

    * 2a1. System requests for correct details.

      Use case resumes from step 1.

* 2b. System detects that the primary phone number or email address is already used by an existing customer,
  including customers hidden by filters. Phone comparison ignores spaces; email comparison ignores letter case.

    * 2b1. System identifies the duplicate contact field and displays an error without adding a customer or changing existing records.

      Use case resumes from step 1.

Shared backup contact numbers are allowed.



**Use case: UC06 - View Customer**

**MSS**

1.  User requests to restore the display using `reset`.
2.  System shows all customers and dogs, plus upcoming activities.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.



**Use case: UC07 - Edit Customer**

**MSS**

1.  User requests to <ins>list customers (UC06)</ins>.
2.  User enters the command, customer details and the customer ID for the customer to be edited.
3.  System validates the command, customer details and positive customer ID, including the uniqueness of the resulting primary phone number and email address against other customers.
4.  System edits the customer and displays a success message.

    Use case ends.

**Extensions**

* 2a. User enters an invalid command format.

    * 2a1. System displays an error message.

      Use case resumes from step 2.

* 3a. System detects invalid customer details or a non-positive customer ID.

    * 3a1. System requests the correct details without changing existing records.

      Use case resumes from step 2.

* 3b. System cannot find the specified customer ID.

    * 3b1. System requests an existing customer ID without changing existing records.

      Use case resumes from step 2.

* 3c. System detects that the resulting primary phone number or email address is already used by another customer,
  including customers hidden by filters. Phone comparison ignores spaces; email comparison ignores letter case.

    * 3c1. System identifies the duplicate contact field and displays an error without changing existing records.

      Use case resumes from step 2.

Customers may retain their own primary phone number and email address. Shared backup contact numbers are allowed.



**Use case: UC08 - Delete Customer**

**MSS**

1.  User requests to <ins>list customers (UC06)</ins>.
2.  User enters the command and the customer id for the customer to be deleted.
3.  System validates the command and customer id.
4.  System deletes the customer and displays a success message.

    Use case ends.

**Extensions**

* 1a. User enters an invalid command format.

    * 1a1. System displays an unknown-command message.

      Use case resumes from step 1.

* 2a. System cannot find the specified customer id.

    * 2a1. System requests for an existing customer id.

      Use case resumes from step 2.



### Non-Functional Requirements

NFR-01. **Platform compatibility**: Should work on any _mainstream OS_ as long as it has Java `25` or above installed.

NFR-02. **Offline operation**: After installation and setup, all core dog and customer management operations shall work without an internet connection.

NFR-03. **Performance**: With up to 500 dog records and 500 customer records, WatchDog shall display the result of an add, edit, delete, list or search command within 2 seconds of submission on the agreed test laptop.

NFR-04. **Startup time**: With the same dataset, WatchDog shall load its saved records and become ready to accept commands within 10 seconds of launch on the agreed test laptop.

NFR-05. **Keyboard usability**: After launch, users shall be able to perform all core dog and customer management operations using the keyboard without requiring mouse interaction.

NFR-06. **Typing efficiency**: A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

NFR-07. **Error clarity**: For invalid command syntax, missing required fields or invalid field values, WatchDog shall display an English error message identifying the problem and providing the expected format or accepted values.

NFR-08. **Robustness**: An invalid command or an input that fails validation shall not terminate the application or change existing records. The application shall remain ready to accept the next command.

NFR-09. **Data persistence**: Following successful data changes and a normal application exit, WatchDog shall restore the updated dog records, customer records and their associations when reopened.

NFR-10. **Privacy**: WatchDog shall store dog and customer records locally and shall not transmit their contents to external services.

### Glossary

* **WatchDog**: The application used to manage a dog daycare centre's customer and dog information.
* **User**: The person operating WatchDog, primarily the dog daycare manager.
* **Customer**: A person whose contact details are recorded because they use the daycare's services for their dog or dogs.
* **Dog record**: The information stored about an individual dog, including its ID, name, breed and size.
* **Customer record**: The information stored about a customer, including their ID, name, primary contact number, email and backup contact number.
* **Dog ID**: A unique integer assigned by WatchDog to identify a dog record. Dogs with the same name are distinguished by their IDs.
* **Customer ID**: A unique integer assigned by WatchDog to identify a customer record. Customers with the same name are distinguished by their IDs.
* **List index**: A record's position in the currently displayed list. Unlike a record ID, this position may change when the list is filtered or reordered.
* **Dog-customer association**: The recorded relationship indicating which customer owns a dog.
* **Primary contact number**: The customer's main telephone number, used as the first contact option.
* **Backup contact number**: An alternative telephone number used when the customer cannot be reached through their primary number.
* **Breed**: The recorded breed or breed mix of a dog, such as Chihuahua, Shiba Inu or a mixed breed.
* **Dog size**: The dog's recorded size category: small, medium, large or giant, according to the size chart adopted by the team.
* **Scheduled attendance**: A planned visit by a dog to the daycare. A scheduled dog is not necessarily currently present.
* **Check-in / Check-out**: Recording a dog's arrival at or departure from the daycare.
* **Present dog**: A dog that has checked in and has not yet checked out.
* **Activity**: A care task planned for a dog, such as feeding, grooming or exercise.
* **Aggressiveness**: Recorded tendency to show threatening or potentially harmful behaviour towards humans or other dogs, including any known triggers.
* **Temperament**: A dog's usual behavioural tendencies, such as being calm, shy or friendly. This is broader than aggressiveness.
* **Care instructions**: Recorded directions for looking after a particular dog, including feeding, exercise and special precautions.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with sample customers. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Daycare summary

Use a disposable data folder for this test. Substitute the computer's current local date for
`TODAY`, the preceding date for `YESTERDAY`, and the following date for `TOMORROW`.

1. With no records, run `summary`. Expect zero for all six counts and today's date.
1. Add two dogs and one customer; leave both dogs unowned and the customer without dogs.
   Add five schedules: one-time yesterday, today and tomorrow, daily starting yesterday, and
   weekly starting today. Use non-overlapping times for the today and daily schedules.
1. Enrol the first dog in the one-time today schedule and the daily schedule on `TODAY`.
   Enrol the second dog in the daily schedule on `TOMORROW`. Leave the weekly activity empty.
1. Run `summary`. Expect `2 dogs | 1 customers`, `5 total | 4 from today onwards`, and
   `3 activities | 1 distinct dogs enrolled` for today, even after today's sessions have ended.
1. Hide records using search/filter commands and run `summary` again. Expect identical counts
   and unchanged panels. Run `summary extra`; expect the usage error and unchanged data/view.
1. Delete the first dog and run `summary` again. Expect one dog and zero dogs enrolled today;
   the customer and all five schedules remain counted.
1. At the minimum window size and normal/larger display scaling, verify that all three feedback
   lines can be read, scrolling within the result area if needed. Repeat with larger counts.

Also verify mutation → undo → summary → redo: summary preserves the redo operation and does not
appear as an undoable mutation.

### Undo and redo

1. Add a dog, run `help`, then `undo`. The dog disappears; `help` is not the undone command.
1. Run `help`, then `redo`. The dog returns. Repeat with `summary` instead of `help`.
1. Undo again, then add another dog. `redo` reports no command to redo and does not replace the new dog.
1. Link two dogs to different customers. Run `searchd n/Buddy`, `reset`, then `undo`.
   Only Buddy and Buddy's owner appear. Repeat using `searchc n/Alice`; only Alice and her dog appear.
1. With disposable data, make the data file unwritable after a successful add, then run `undo`.
   The save reports an error, but the dog disappears from memory. Restore write access and run `redo`;
   the dog returns and saves. Repeat for a failed `redo`, a new add after `undo`, and `clear`.
   A failed `clear` leaves memory empty and neither history operation available.

### Deleting a dog

1. Add a dog with `adddog n/Buddy b/Poodle s/small` and note its displayed ID.
1. Run `delete dog ID` with that ID. The dog and all of its activity enrolments, including past occurrences,
   are removed; other dogs' enrolments and customer records remain intact.
1. Repeat the command. It reports an invalid dog ID without changing stored records.
1. Try `delete 1`. The retired person command reports an unknown command.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
