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

The diagrams below describe the implemented design. Class diagrams are partial views: they omit
unrelated members and utility classes. Solid arrows show navigable associations, dashed arrows show
dependencies, and hollow triangles point to a superclass or implemented interface. Labels explain
the purpose of a dependency where needed.

### Persistent identities

Customers, dogs and activities use persistent IDs as their identities. These IDs are assigned by the
WatchDog and are stored with the records, so they remain stable across application restarts and
are not affected by filtering, list positions or deletion of other records. This allows commands and
relationships to refer to a record reliably even when the visible list changes.

### Customer–dog ownership

`LinkCommandParser` accepts `c/CUSTOMER_ID d/DOG_ID [DOG_ID]...`: one customer field followed by
one dog field containing distinct, whitespace-separated IDs. Repeated `d/` prefixes are rejected.
`LinkCommand` delegates the batch to `Model.linkDogs`; `ModelManager` forwards to `WatchDog`.
The WatchDog looks up persistent IDs in the complete lists and validates every endpoint and ownership
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
`UnlinkException` into command feedback. `WatchDog` validates every selected dog exists and has an owner
before publishing one replacement list of unowned copies. Dogs may belong to different customers.
The customer form validates the customer, selects all their dogs from the complete list, and uses the
same atomic batch operation. Missing customers and customers with no linked dogs are rejected.
Unlinking preserves records and filters, including a customer's last dog, and uses the existing nullable
`ownerId` storage field without a schema change.

`WatchDog.removeCustomer` clears ownership of that customer's dogs, regardless of filters,
preserving their persistent IDs and details. Unowned dog cards show a person icon followed by `None`.
Removing a dog leaves its customer intact. Customer edits must preserve persistent IDs to keep links valid.
Observable list updates refresh dog cards, which display customer IDs without changing search filters.
Customer cards display owned dog IDs below backup contact. `CustomerListPanel` observes the complete dog list
as well as visible customers, so these IDs remain accurate when dog cards are filtered out.

`JsonAdaptedDog` persists `ownerId`. A missing or null field means unlinked, preserving compatibility
with older files. Negative owner IDs and references to missing customers are rejected on load;
customers are restored before dogs. Copies of WatchDog retain ownership through immutable dogs.
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

The conceptual class diagram below summarises these domain relationships. Each enrolment belongs
to one dog and one activity schedule, with its date identifying the occurrence. A dog can have at most
one owner, and each dog or activity can have many dated enrolments.

<puml src="diagrams/DomainClassDiagram.puml" alt="Conceptual ownership and dated activity enrolment relationships" />

These lines describe domain relationships, rather than Java object references. In the implementation,
`Dog` stores `ownerId`, and `Enrolment` stores `dogId`, `activityId`, and `date`. The model resolves these
IDs against the complete lists. The model diagrams below show that storage structure separately.

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" alt="Dependencies between the four main application components" />

The architecture diagram shows dependencies between the four main components. `MainApp` wires
them together at startup. `Commons` supplies shared utilities; its dependency arrows are omitted.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `deletedog 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" alt="Successful dog deletion and saving at component boundaries" />

This is a component-level view: calls made by commands inside `Logic` are represented at the
`LogicManager` boundary. Observable-list notifications are omitted. For this undoable deletion,
`LogicManager` captures a model snapshot before execution and updates history before saving.
Ordinary successful commands, including queries, use the same save path; a save failure is reported
as a command error. History-neutral `help`, `summary`, and `exit` still return their results if saving fails.
The diagram shows the successful execution and save path. Clear confirmation
and undo/redo dispatch are described separately below.

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" alt="Component interfaces and their manager implementations" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, including `CommandBox`, `ResultDisplay`, separate customer,
dog and activity list panels, and `TodayActivityListPanel`. `TodayActivityListPanel` reuses the activity-list
implementation and filters it to schedules occurring today. Visible GUI parts inherit from the abstract
`UiPart` class, which provides their shared FXML-loading behavior.

Each list panel observes its model data and uses `CardListUpdater` to reconcile stable, ID-keyed card factories.
Unchanged entries are retained instead of rebuilding the entire list, which preserves the visible record during
ordinary edits, ownership changes, and enrolment changes. Commands that intentionally replace the displayed
result set, such as search, filter, and reset, scroll the panels to the first result. JavaFX still creates
card nodes only as cells need them. Customer ownership summaries use the complete dog list independently of
the current dog filter.

Customer and dog cards install their tooltips and copy menus through `CardInteractionUtil`. Clipboard writes
return a success result through `ClipboardWriter`, so the UI shows `Copied!` only after a successful write and
shows failure feedback otherwise.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Customer` and `Dog` objects from the model.

The general activity panel shows distinct dog IDs booked today or later, alongside search-specific
per-dog enrolment dates when a search context supplies them. The Today panel uses all schedules and
shows only schedules and bookings on the current date. `ActivityBookingSummary` formats these booking
lines from live enrolments. Activity, enrolment, and search-context changes trigger refreshes; after a
successful command, `MainWindow` captures one date and refreshes both activity panels with it.
There is no midnight scheduler; the date is re-evaluated on the next refresh.

The UI diagram omits the common `UiPart` superclass, JavaFX internals, and card interaction helpers
to focus on panels, card creation, and booking summaries.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" alt="Logic interfaces, command execution, and representative commands" />

The sequence diagram below expands the parsing stage of `LogicManager.execute("deletedog 1")`.
The returned command is then executed by `LogicManager`. Model interactions and saving are shown
in the architecture sequence.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `deletedog 1` Command" />

This sequence shows parser and command construction at the point they occur. Activation bars and
temporary-object disposal are omitted. Other feature sequences begin with an already parsed command;
they reuse this execution path and the save path shown in the architecture sequence.

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `WatchDogParser` object, which in turn creates a parser that matches the command (e.g., `DeleteDogCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteDogCommand`). For an ordinary undoable command, `LogicManager` captures the model state before executing it. Undo, redo, and clear confirmation follow the special paths described below.
1. The command communicates with the `Model` when it is executed (e.g. to find and delete a dog by persistent ID). These interactions are omitted from this Logic view and shown at component level in the architecture sequence.
1. Successful execution produces a `CommandResult`. `LogicManager` updates history and saves the model before returning the result; `help`, `summary`, and `exit` preserve history.

The following diagram expands a representative parser and the utilities it uses:

<puml src="diagrams/ParserClasses.puml" alt="Representative add-dog parser and its parsing utilities" />

How the parsing works:
* For commands requiring field parsing, `WatchDogParser` creates the appropriate parser. The diagram uses `AddDogCommandParser` as a representative example: it tokenises the arguments, validates values with parsing utilities, and creates an `AddDogCommand` containing a dog without an allocated persistent ID.
* Concrete command parsers implement `Parser` and return command objects. The diagram omits the other parsers; some commands, such as `summary`, are constructed directly by `WatchDogParser` after checking their arguments.

`help [COMMAND]` is parsed by `HelpCommandParser`, which looks up normalized, possibly multiword
topics in `CommandHelp` and creates a `HelpCommand`. Bare `help` opens the help window; topic help
returns command guidance through normal result feedback. Both forms preserve undo/redo history.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" alt="Model interfaces, stored records, and filtered views" />

This implementation view shows stored collections separately from filtered views. Ownership and
enrolment references use ID attributes; there are no direct `Dog`-to-`Customer` or
`Enrolment`-to-`Activity` object references. The activity detail view below expands the schedule fields.


The `Model` component,

* stores customers and dogs in `UniqueCustomerList` and `UniqueDogList`, with persistent IDs and dog ownership links.
* stores activity schedules and dated enrolments separately; filtered activities are a view of the stored schedules.
* exposes separate unmodifiable observable customer and dog lists for search and category filtering. The UI observes these lists to update cards when records or filters change.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)



### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T15-4/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" alt="Storage interfaces, JSON file access, and model dependencies" />

The `Storage` component,
* can save both WatchDog data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonWatchDogStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Removal of the legacy person feature

WatchDog uses customer and dog records. The original AB3 person records, tags, person-only
commands (`add`, `edit`, `delete INDEX` and `find`), person storage adapters and unused list panels
have been removed. Shared components such as `WatchDog`, preferences, parsing utilities,
help, `clear` and JSON storage remain in use.

Existing JSON files may contain a legacy `persons` field. It is ignored on loading and omitted
on the next save; its records are not converted to customers. Customer/dog records, ownership
links and both next-ID counters are preserved. A file containing only legacy persons loads
as an empty WatchDog book. No startup sample person records are created.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

Feature sequence diagrams show successful command execution through the `Model` API, with
`ModelManager` as the concrete participant. Parsing and saving follow the shared flows above.
Model validation and list updates are summarised in notes; their internal calls and exception
paths are omitted. The summary query is a separate view of the model's internal ID resolution.

### Adding dogs

`AddDogCommandParser` validates the dog details supplied by the user and creates an
`AddDogCommand`. The command delegates to `Model`, which forwards the request through
`ModelManager` to `WatchDog`.

`WatchDog` assigns the dog a persistent ID when it is added. The ID is allocated from a
monotonically increasing counter rather than from the dog's position in the list, so deleting a dog
does not change the identities of the remaining dogs and deleted IDs are never reused. The counter
and dog IDs are persisted so that new dogs continue to receive unique IDs after the application
restarts.

Dog IDs remain positive `int` values, while the next-ID counter is a `long`. A counter value of
`Integer.MAX_VALUE + 1` represents exhaustion; `adddog` then fails without changing the stored dogs
or counter. A missing counter in legacy data is derived as one more than the largest stored dog ID,
or one for an empty dog list. An explicit stored counter must be greater than every stored dog ID and
cannot exceed the exhaustion value. Zero, negative, fractional, string-valued, out-of-range, or
otherwise inconsistent counters are rejected rather than silently repaired. A counter may validly be
more than one above the largest current dog ID because higher-ID dogs might previously have been
deleted.

The following diagram shows the add-dog command's interaction with the model. The important part is that the
persistent dog ID is assigned by `WatchDog` only
after the command reaches the model layer.

<puml src="diagrams/AddDogSequenceDiagram.puml" alt="Adding a dog and receiving its persistent ID from the model" />

### Editing dogs

`EditDogCommandParser` parses the persistent dog ID and the fields to be edited. The command uses
the ID to locate the dog in the complete dog list rather than relying on a filtered-list position.
`WatchDog` validates the replacement details and publishes the updated dog while preserving its
persistent ID and current owner relationship.

The following diagram shows the edit-dog command flow. The command locates the dog using its
persistent ID, so editing remains independent of the current
filtered-list position. The replacement dog retains both its ID and owner relationship.

<puml src="diagrams/EditDogSequenceDiagram.puml" alt="Editing a dog using its persistent ID" />

### Retired list commands

The former list dog and list customer commands are no longer routed by WatchDogParser.
Use reset to restore all customers and dogs and upcoming activities, or searchc/searchd
to narrow the display. Retired list-dog source and tests are preserved under unused/@xandergoh/;
list-customer source, parser, and tests are under unused/@its-shoutime/. These archives are
excluded from Gradle source and test sets and retain authorship annotations.

### Retired select commands

The former select dog and select customer implementations are retained under
unused/@its-shoutime/ for authorship reference only. They are not routed by the active
WatchDogParser and are not supported by the final product.

### Undo and redo

`WatchDogParser` accepts `undo` and `redo` only without arguments. `LogicManager` handles these
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

`Model.createState()` copies the `WatchDog`, including its records and all three next-ID counters, and captures
the active customer, dog and activity predicates and the enrolment-summary dog predicate. `Model.restoreState()`
resets the existing WatchDog and reapplies the saved display state. Reusing the existing model
and observable lists lets UI listeners continue to see updates after a restoration. It refreshes
predicates twice: the first pass installs source filters, and the second re-evaluates dependent filters.
This handles both dog filters whose customer predicate reads filtered dogs and customer filters whose
dog predicate reads filtered customers.

On `undo`, `LogicManager` captures the current state for redo, restores the undo snapshot, moves
the history slots, then saves WatchDog. `redo` symmetrically captures the current state for
undo, restores the redo snapshot, moves the slots, and saves. An ordinary command updates history
immediately after successful execution and before saving. This ordering matches the existing policy
of retaining in-memory changes when a save fails: the corresponding inverse remains available,
and `clear` still invalidates history. The disk may contain an older state, or a partially written
state if an actual write had started. Missing history produces a `CommandException`; save failures
are reported as command errors. History is held only in memory and is unavailable after a restart.

`LogicManagerTest` and `LogicManagerHistoryTest` cover the undo/redo cycle, neutral commands,
invalidated history, `clear`, failed commands, dependent filters, and save failures.
`ModelManagerTest` checks restoration of data, ID counters, relationships, filters, and selection, while
`WatchDogParserTest` checks argument validation.

### Daycare summary

`WatchDogParser` accepts `summary` with blank arguments only and constructs `SummaryCommand`.
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

The sequence below focuses on this model query. It shows why multiple enrolments count a dog once,
and why hidden dogs still contribute to the summary. Stream operations are explained in notes;
activity schedule counts and feedback formatting are omitted.

<puml src="diagrams/DogsEnrolledOnSequenceDiagram.puml" alt="Resolving distinct dogs enrolled on the summary date from stored IDs" />

Feedback follows the existing `CommandResult` and `ResultDisplay` path. No records, filters or
selection are mutated, and no storage schema changes are needed. `LogicManager` still attempts
to save after successful execution, preserving its existing save-error handling. Summary neither
creates an undo entry nor discards redo history.

### Activity creation and storage

The following partial class diagram expands the activity fields and separately stored enrolments.
An activity represents a reusable schedule; its occurrence dates are calculated from its recurrence rule.
Some parameter and return details are omitted. `nextOccurrenceOnOrAfter(date)` returns an
`Optional<LocalDate>`, containing the next occurrence when one exists.

<puml src="diagrams/ActivityClassDiagram.puml" alt="Activity schedule classes and model integration" />

`WatchDogParser` routes `addactivity` to `AddActivityCommandParser`. The parser requires
`n/`, `date/`, `start/`, `end/`, and `r/` exactly once, rejects unknown alphabetic prefixes,
and validates strict calendar dates, minute-resolution 24-hour times and lowercase recurrence.
The immutable command validates a details snapshot with a placeholder ID. Parsing never allocates
an ID. `execute` calls `Model.addActivity(ActivityName, LocalDate, LocalTime, LocalTime, Recurrence)`
and reports the identified schedule returned by the model. No dog/customer filters change.

`ModelManager` delegates to `WatchDog.addActivity`. The WatchDog validates the complete
schedule before assigning the next ID and publishing it through `getActivityList()`, an unmodifiable
observable view of all schedules. Creation allows identical details and overlapping empty schedules.
The first allocated ID is one; the monotonically increasing counter is stored as a long so that
`Integer.MAX_VALUE + 1` can represent exhaustion without wrapping. ID exhaustion rejects creation.
Copy/reset and equality include activities and the counter. Existing customer/dog APIs are unchanged.

`JsonAdaptedActivity` stores the six schedule fields; dates/times use ISO strings and recurrence uses
its enum name. `JsonSerializableWatchDog` stores `activities` and `nextActivityId`, validates
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
The command uses the `Model` interface, implemented by `ModelManager`. The diagram focuses on the
identified schedule returned by that API; parsing and persistence follow the shared Logic flow.

<puml src="diagrams/AddActivitySequenceDiagram.puml" alt="Creating an activity schedule through the model API" />

### Deleting an activity

`WatchDogParser` routes `deleteactivity` to `DeleteActivityCommandParser`, which accepts one
positive persistent ID. The command finds the matching activity in `Model.getActivityList()` and
passes it to `Model.deleteActivity`. `ModelManager` delegates to `WatchDog.removeActivity`, which
checks that the activity is still stored, removes its enrolments, and then removes the activity.
The next activity ID does not change, so deleted IDs are not reused. After the command returns its
success message, `LogicManager` saves the updated WatchDog.

<puml src="diagrams/DeleteActivitySequenceDiagram.puml" alt="Deleting an activity schedule and its enrolments through the model API" />

### Editing an activity

`WatchDogParser` routes `editactivity` to `EditActivityCommandParser`. The parser requires a
persistent activity ID and at least one replacement field. `EditActivityCommand` finds the stored
activity by ID, builds an immutable replacement with the same ID, and calls `Model.setActivity`.
Fields omitted from the command retain their stored values.

`ModelManager` delegates the replacement to `WatchDog`, which checks that the target matches a
stored activity and retains its ID. Any enrolment referencing that activity rejects the edit with
feedback suggesting a new activity instead. A permitted replacement then passes schedule validation
before being stored. Enrolments are never removed by editing. `LogicManager` saves the address book
after successful execution.

<puml src="diagrams/EditActivitySequenceDiagram.puml" alt="Editing an activity schedule with no enrolments" />

### Dog search

`WatchDogParser` routes `searchd` to `SearchDogCommandParser`. The parser accepts the lowercase fields
`n/`, `b/`, `s/`, and `i/`, rejects duplicate or unknown fields, normalises whitespace, and validates sizes
and positive dog IDs. `DogSearchPredicate` performs case-insensitive substring matching for names and breeds,
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

<puml src="diagrams/SearchDogSequenceDiagram.puml" alt="Searching dogs and related owners and activities" />

### Customer search

`WatchDogParser` routes `searchc` to `SearchCustomerCommandParser`. The parser accepts the optional
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

`WatchDogParser` routes `searcha` to `SearchActivityCommandParser`. The parser accepts at least one
of the lowercase fields `n/`, `d/`, `t/`, `r/`, and `i/`, rejects duplicate or unknown fields, and validates dates
and times using strict `YYYY-MM-DD` and `HH:mm` formats. Recurrence is restricted to `Once`, `Weekly`,
or `Daily`. Names are trimmed, internal whitespace is normalised, and matching is case-insensitive.

`SearchActivityCommand` (invoked by `searcha`) applies an `ActivitySearchPredicate` to the model's filtered activity list.
Name matching uses a case-insensitive substring, date matching delegates to `Activity.occursOn(date)`
so recurring activities are handled correctly, and time matching includes both endpoints.
Recurrence matching compares the activity's stored recurrence. The optional activity ID must be positive
and identify an existing activity, including one hidden by filters. Invalid or nonexistent IDs produce
`Error in searching activities:` followed by `Please enter a valid activity index` without changing the display.
ID matching is exact. Multiple fields are combined with AND semantics.
 The activity panel observes this filtered list, while the
underlying stored schedules remain unchanged. The success message reports only the supplied fields, in the
order name, date, time, recurrence, and activity ID.
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

`WatchDogParser` routes the exact lowercase command `today` to `TodayCommandParser`, which rejects
any additional arguments. `TodayCommand` delegates the filtering behavior to `SearchActivityCommand`
with `LocalDate.now()` as the date criterion. This reuses the existing recurrence-aware activity
matching and derives dogs from enrolments on today's exact occurrence, followed by their owners.
The command replaces the active activity, dog, and customer displays and returns
`Successfully displayed today's activities, as well as the dogs and customers involved for the day`.

### Unlinked filtering

`WatchDogParser` routes `filterun` to `FilterUnlinkedCommandParser`, which accepts only the lowercase
categories `dog` and `customer`, with arbitrary surrounding whitespace. `FilterUnlinkedCommand` filters dogs
whose `ownerId` is absent or customers with no owned dogs. The dog form hides customers and shows
activities with bookings for unowned dogs; its enrolment details include only those dogs. The customer
form hides both dogs and activities. Its predicates are stored by `ModelManager`, so the results refresh
after later record or relationship changes.

<puml src="diagrams/FilterUnlinkedSequenceDiagram.puml" alt="Filtering unlinked dogs or customers" />

### Activity enrolment

Dogs are linked to activity occurrences through separate `Enrolment` records. Each record stores an
activity ID, a dog ID, and an occurrence date, allowing an activity to have multiple dogs without
storing dog objects directly inside the reusable activity schedule.

Users can enrol or unenrol one or more dogs using:

```text
enrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]
unenrol a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]
```

The parsers accept the three required fields in any order and convert the activity ID, occurrence date, and
whitespace-separated dog IDs into a command. Each field must appear exactly once, and activity and dog IDs must
be positive. Parser errors identify the missing, invalid, repeated, or malformed field and append the command's
complete format and example. Execution errors append the same guidance.
The command delegates through `Model` and `ModelManager` to `WatchDog`, which verifies that the
activity and dogs exist, the date is a valid occurrence and is today or later, and the requested
enrolments are valid before updating the enrolment list. A dog cannot be enrolled in another activity
with an overlapping time on the same date; adjacent activities are allowed. Batch operations are
atomic. Successful mutations are logged at `FINE` and guarded by postcondition assertions. Enrolment
changes are persisted with WatchDog.

The activity-card overview follows a view policy rather than changing the schedule model. `Logic` exposes
`getEnrolmentList()` as the same live unmodifiable observable view supplied by `WatchDog`; no dog IDs are
stored redundantly in `Activity`. `MainWindow` passes that view to both activity panels. The general panel
formats distinct, numerically sorted dog IDs from enrolments whose date is today or later, while the Today
panel uses only enrolments whose date equals the captured local date. Existing address-book invariants ensure
that these enrolments refer to stored activities and dogs.

`ActivityBookingSummary` performs the date query and formatting without JavaFX. `ActivityListPanel` captures
`LocalDate.now(clock)` once per refresh, reconciles cards when either the displayed activities or enrolments
change, and accepts an injectable clock for deterministic tests. The Today variant additionally displays only
schedules that occur on that captured date. Because reset, clear, undo and redo update the existing observable
lists, listeners remain attached. `MainWindow` also requests a refresh after each successful command, so date
rollover is reflected on the next successful command; no midnight scheduler is installed.

`ActivityCard(Activity, String)` receives the complete overview label. It presents `Schedule starts` separately
from the booking summary and never substitutes an enrolment date for `Activity.startDate`. Xander's search-context
work can extend the supplied presentation text or add per-dog date lines without changing the baseline live-list
plumbing or storing view state in `Activity`.

<puml src="diagrams/EnrolDogSequenceDiagram.puml" alt="Enrolling dogs into an activity" />

Unenrolment follows the same parser and model delegation path. After validating the activity, date,
dogs, and exact dated enrolments, WatchDog removes only the matching activity/dog/date records
instead of changing the activity schedule itself. Past dates may be unenrolled for correction. Successful
removal is logged and checked with a postcondition assertion.

<puml src="diagrams/UnenrolDogSequenceDiagram.puml" alt="Unenrolling dogs from an activity" />

`ActivityEnrolmentTest` covers recurrence validation, atomic batches, overlap boundaries and error details;
the enrol and unenrol command/parser tests cover command construction and malformed or impossible dates.
`EnrolmentStorageTest` covers persisted and legacy records. `DeleteDogCommandTest` checks that deleting a dog
removes both past and future enrolments without affecting other dogs. `ListPanelsTest` and `MainWindowTest`
cover the today-only activity filter and its UI integration. `ActivityBookingSummaryTest` covers empty summaries,
date boundaries, activity isolation, deduplication and numeric sorting with fixed dates.

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

`LogicManager` records each command. `WatchDog` logs successful enrolment changes and dog deletion at
`FINE`, including the number of enrolment records removed. An invalid enrolment restored from storage is
logged at `WARNING` with its IDs, date, and validation reason before the load error is propagated. Expected
user validation failures are returned through command feedback rather than logged as warnings, avoiding
misleading error logs for routine input mistakes.

`AddActivityCommandParserTest`, `AddActivityCommandTest`, `ActivityIntegrationTest` and
`ActivityStorageTest` cover parser failures, unchanged state, observable access, identity boundaries,
all recurrence modes, old files and command-to-disk round trips using temporary files.

### Search commands

The active category searches are `searcha`, `searchc`, and `searchd`. Each parser accepts its documented field prefixes and constructs the corresponding field-based command. The commands replace the relevant filtered observable list and keep related dogs, customers, and activities consistent.

`SearchDogCommandParser` accepts `n/`, `b/`, `s/`, and `i/`; `SearchCustomerCommandParser` accepts `n/`, `p/`, `e/`, `d/`, `bc/`, and `i/`; and `SearchActivityCommandParser` accepts `n/`, `d/`, `t/`, `r/`, and `i/`. Unsupported prefixes, duplicate fields, invalid IDs, invalid sizes, and malformed values are rejected before execution.

The global search command and its category compatibility routes have been retired. Use the field-based searchc, searchd, and searcha commands (i/ for persistent IDs). Retired global search code, tests, and orphaned helpers are preserved under unused/@Kenneth-Chia/. The list commands are also retired; use `reset` to restore all customers and dogs and upcoming activities.

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

* Is a dog day care centre manager in Singapore
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
| `* * *`  | Dog day-care manager managing multiple dogs            | Schedule activities for the dogs                                           | I can keep track of upcoming activities                           |
| `* * *`  | Dog day-care manager preparing for the day             | View all activities scheduled for a particular day                         | I know what needs to be prepared each day                         |
| `* * *`  | Dog day-care manager                                   | Record when a dog checks in and checks out                                 | I can accurately track which dogs are currently present           |
| `* * *`  | Dog day-care manager entering information              | View clear error messages when invalid information or commands are entered | I can correct mistakes without crashing the program               |
| `* *`    | Dog day-care manager                                   | Mark scheduled activities as completed                                     | I can keep track of which tasks have been carried out             |
| `* *`    | Forgetful dog day-care manager                         | Maintain a checklist for each dog                                          | I can ensure that all required tasks are completed                |
| `* *`    | Dog day-care manager                                   | View all dogs belonging to a specific customer                             | I can see every dog owned by that customer                        |
| `* *`    | Busy dog day-care manager                              | Filter dogs by characteristics such as size or breed                       | I can quickly find dogs with specific characteristics             |
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

The following stories also describe needs supported by the current version. Unimplemented stories above
remain requirements for future iterations; priority does not indicate implementation status.

| Priority | As a/an | I want to... | So that I can... |
|----------|---------|--------------|-----------------|
| `* * *` | Dog day-care manager | Remove ownership without deleting dogs or customers | I can correct ownership information |
| `* * *` | Dog day-care manager | Enrol and unenrol several dogs in a dated activity occurrence | I can manage bookings efficiently |
| `* * *` | Dog day-care manager | Reject a whole batch booking if any dog has a conflicting activity | I can avoid double bookings without partially applying my request |
| `* *` | Dog day-care manager | Find dogs without owners and customers without dogs | I can identify incomplete ownership records |
| `* *` | Dog day-care manager | Delete an activity and its bookings | I can remove cancelled schedules |
| `* *` | Dog day-care manager | View overall counts and today's distinct enrolled dogs | I can assess today's workload |
| `* *` | Dog day-care manager | Search activities by name, date, time, recurrence, or ID | I can locate schedules and their booked dogs and owners |
| `* *` | Dog day-care manager | Restore the full display after searching | I can return to my overview |
| `* *` | Dog day-care manager | Confirm or cancel clearing all records before it executes | I can deliberately reset my data while avoiding accidental loss |
| `* *` | Dog day-care manager | Redo a command I just undid | I can restore an intended change |
| `* *` | Dog day-care manager | Copy card details and read shortened values in full | I can reuse accurate information without retyping |
| `* *` | New dog day-care manager | View help for a specific command | I can learn its format while keeping my current view |
| `* * *` | Dog day-care manager | Recover saved records after restarting | I can continue managing the daycare across sessions |

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

1.  User requests to <ins>view dogs (UC02)</ins> using `reset`.
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

1.  User requests to <ins>view dogs (UC02)</ins> using `reset`.
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

1.  User requests to <ins>view customers (UC06)</ins> using `reset`.
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

1.  User requests to <ins>view customers (UC06)</ins> using `reset`.
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



**Use case: UC09 - Edit Activity**

**MSS**

1.  User enters the command, the persistent activity ID and one or more activity details to edit.
2.  System validates the command, activity ID and resulting activity details, and checks that the activity has no enrolments.
3.  System updates the activity schedule, preserving its ID and unspecified details, and displays a success message.

    Use case ends.

**Extensions**

* 2a. User enters an invalid command format or activity ID format, or provides no fields to edit.

    * 2a1. System displays an error message without changing existing records.

      Use case resumes from step 1.

* 2b. System detects invalid activity details, including an end time that is not later than the start time on the same day.

    * 2b1. System requests the correct details without changing existing records.

      Use case resumes from step 1.

* 2c. System cannot find the specified activity ID, including in activities hidden by filters.

    * 2c1. System displays an error requesting an existing activity ID without changing existing records.

      Use case resumes from step 1.

* 2d. System detects that the activity has enrolments, including past occurrences.

    * 2d1. System rejects the edit and asks the user to create a new activity instead, without changing existing records.

      Use case ends.

For recurring activities, the edit applies to the entire schedule rather than a single occurrence.



**Use case: UC10 - Delete Activity**

**MSS**

1.  User enters the command and the persistent activity ID for the activity to be deleted.
2.  System validates the command and activity ID and locates the activity, including activities hidden by filters.
3.  System deletes the activity schedule and all its dog enrolments, including past occurrences, and displays a success message.

    Use case ends.

**Extensions**

* 2a. User enters an invalid command format or activity ID format.

    * 2a1. System displays an error message without changing existing records.

      Use case resumes from step 1.

* 2b. System cannot find the specified activity ID.

    * 2b1. System displays an error requesting an existing activity ID without changing existing records.

      Use case resumes from step 1.

For recurring activities, deletion removes the entire schedule rather than a single occurrence.
Dog and customer records, other activity schedules and their enrolments are preserved.



**Use case: UC11 - Clear all records with confirmation**

**MSS**

1. User enters `clear`.
2. System warns that clearing cannot be undone and requests Y/N confirmation.
3. User enters `Y`.
4. System clears all records and undo/redo history, preserves active filters, and attempts to save.

   Use case ends.

**Extensions**

* 3a. User enters `N`: system cancels without changing records or history. Use case ends.
* 3b. User enters anything other than Y/N: system requests Y/N again without executing another command.
  Use case resumes from step 3. Answers ignore case and surrounding whitespace.
* 4a. Saving fails: system reports the error; memory remains cleared and history remains unavailable.
  Use case ends.

**Use case: UC12 - Link a batch of dogs to a customer**

**MSS**

1. User supplies one customer ID and one or more distinct dog IDs using `link`.
2. System validates the entire batch, including existence of every record and absence of existing owners.
3. System assigns every selected dog to the customer, refreshes the active view, and attempts to save.

   Use case ends.

**Extensions**

* 2a. The format or an ID is invalid, a record is missing, or any dog already has an owner:
  system reports the problem without changing any ownership. Use case resumes from step 1.
* 3a. Saving fails: system reports the error; ownership changes remain in memory and can be undone.
  Use case ends.

`unlink d/ID [ID]...` follows the same atomic pattern, requiring every dog to exist and have an owner,
then removing their ownership. Dogs may have different owners. `unlink c/ID` instead selects all dogs
of one existing customer; a customer with no linked dogs is rejected. Neither form deletes records.

**Use case: UC13 - Enrol a batch of dogs in an activity occurrence**

**MSS**

1. User supplies an activity ID, an occurrence date, and distinct dog IDs using `enrol`.
2. System validates all records, that the date is today or later and occurs in the schedule, and that
   none of the dogs is already enrolled or has an overlapping booking on that date.
3. System creates all requested dated enrolments, refreshes the view, and attempts to save.

   Use case ends.

**Extensions**

* 2a. A field or record is invalid, the date is invalid, or any dog fails the booking checks:
  system reports the problem without creating any requested enrolment. Use case resumes from step 1.
* 3a. Saving fails: system reports the error; enrolments remain in memory and can be undone.
  Use case ends.

`unenrol` follows the same atomic pattern but requires every exact activity/dog/date booking to exist
and removes those bookings. It permits past occurrence dates so historical mistakes can be corrected.
Adjacent activities whose end and start times are equal do not conflict.

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
* **Activity**: A care task planned for a dog, such as feeding, grooming or exercise.

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

### Adding and editing dogs

1. Run `adddog n/O'Neil-Jr b/German-Shepherd s/medium`.
   Expect one dog to be added with a positive persistent ID, with the apostrophe and hyphens preserved.
1. Omit each required field in turn, and repeat with an empty value such as
   `adddog n/ b/Poodle s/small`. Expect the message to identify the missing or empty field and then show
   the full `adddog` format, example, and valid size options. No dog is added.
1. Repeat with an unsupported size, a repeated prefix, text before the first prefix, or punctuation that is
   invalid for the relevant field. Expect a specific validation or format error followed by the same shared
   guidance. No dog is added.
1. Run `editdog ID n/Sir-Barks-a-Lot b/Mixed-Breed` using the ID from step 1. Expect the supplied fields to
   change while the dog's size, customer link, persistent ID, and enrolments remain unchanged.
1. Run `editdog 0 s/large`, use a missing positive ID, omit all edit fields, and provide an empty field.
   Expect the relevant ID or field error followed by the full `editdog` format, example, and size options.
   The original dog remains unchanged.

### Enrolling and unenrolling dogs

Use an existing activity occurrence on `TODAY` or a future date and two existing positive dog IDs.

1. Run `enrol d/DOG_ID_1  DOG_ID_2 date/YYYY-MM-DD a/ACTIVITY_ID`, using multiple spaces and a field order
   different from the documented format. Expect both dogs to be enrolled in that occurrence.
1. Repeat the same enrolment. Expect the command to report that the dogs are already enrolled; no duplicate
   enrolment records are created.
1. Try a missing or empty field, a repeated prefix, `a/0`, `d/0`, a comma-separated dog list, duplicate dog
   IDs, an invalid date, and a past date. Expect the message to identify the problem and then show the full
   `enrol` format and example. No enrolments change.
1. Run `unenrol date/YYYY-MM-DD d/DOG_ID_1  DOG_ID_2 a/ACTIVITY_ID` with the fields reordered.
   Expect only those dated enrolments to be removed. Repeat the command and expect an error without other changes.
1. With both dogs now unenrolled from the first activity, enrol only the second dog in another activity
   that overlaps the first on the same date. Try enrolling both dogs in the first activity, placing the
   unbooked dog first in `d/`. Expect the entire batch to fail: neither dog gains a booking in the first
   activity, and the second dog's other booking remains. Remove that other booking before continuing.
1. Repeat the invalid-field checks for `unenrol`. Also verify that a valid past occurrence can be unenrolled,
   unlike enrolment, so historical mistakes can be corrected.

### Card details, copying, and scrolling

1. Add or edit a customer to have a long email, and a dog to have a long breed. Hover over each shortened detail.
   Expect a wrapped tooltip containing the complete value.
1. Right-click a customer card. Copy each contact field and then all details, pasting each result into a text
   editor. Expect exact plain-text values and labelled multiline output for all details.
1. Right-click a linked dog card and copy its customer ID. Unlink the dog and reopen the menu.
   Expect the customer-ID item to be disabled and the all-details output to show `Customer ID: None`.
   Relink the dog and expect the action and updated ID to return without restarting the app.
1. Scroll each populated panel to a middle record and edit, link, unlink, enrol, or unenrol a visible record.
   Expect the visible record to remain anchored without a jump. Run a search, filter, or reset command;
   expect each replaced result set to start at its first record.

### Deleting a dog

1. Add a dog with `adddog n/Buddy b/Poodle s/small` and note its displayed ID.
1. Run `deletedog ID` with that ID. The dog and all of its activity enrolments, including past occurrences,
   are removed; other dogs' enrolments and customer records remain intact.
1. Repeat the command. It reports an invalid dog ID without changing stored records.
1. Try `delete 1`. The retired person command reports an unknown command.

### Customer records and ownership

Use a disposable dataset with no customers using the contacts below. Create two unowned dogs with
`adddog n/TestBuddy b/Poodle s/small` and `adddog n/TestCookie b/Beagle s/medium`.
Replace the uppercase ID placeholders below with the persistent IDs reported by the app, not list positions.

1. Run `addcustomer n/Alice Tan p/91234567 e/alice@example.com bc/98765432` and
   `addcustomer n/Ben Lim p/92345678 e/ben@example.com bc/98765432`.
   Expect separate IDs; sharing the backup number is allowed.
1. Try adding another customer with Alice's primary phone, then with `e/ALICE@example.com` and a fresh phone.
   Expect duplicate-contact errors and no addition, even after hiding Alice with `searchc n/Ben`.
   Try a missing field and a seven-digit phone; expect errors without adding records.
1. Run `editcustomer CUSTOMER_ID n/Alice Koh` using Alice's ID. Expect the same ID and unchanged unspecified
   fields. Try changing Alice's primary phone to Ben's, then omit all edit fields; expect errors and no changes.
1. Run `link c/CUSTOMER_ID d/DOG_ID_1 DOG_ID_2`. Expect both dogs to show Alice's ID and Alice's card to list
   both dog IDs, including when the dogs are filtered out. Run `reset` to inspect all cards.
1. Run `unlink d/DOG_ID_1 DOG_ID_2`, then link only the second dog to Ben.
   Try linking both to Alice with the unowned dog first. Expect failure without assigning either dog to Alice.
   Try duplicate dog IDs and `link d/DOG_ID_1 c/CUSTOMER_ID`; expect rejection without changes.
1. Link the first dog to Alice. Run `unlink d/DOG_ID_1 DOG_ID_2`; expect both ownership links to disappear
   despite their different owners. Repeat; expect failure without changing records.
1. Link both dogs to Alice and run `unlink c/CUSTOMER_ID`. Expect both dogs to remain but have no owner.
   Repeat for Alice, then try a nonexistent customer ID; expect errors without changes.
1. Relink both dogs to Alice. Hide Alice with `searchc n/Ben`, then run `deletecustomer CUSTOMER_ID`.
   Run `reset`. Expect Alice to be gone and both dogs to remain unowned, retaining their IDs and details.
   Ben remains. Repeat the deletion; expect an invalid-ID error.

### Activity records and booking cards

Use today's local date in place of `TODAY`, create an unowned dog, and note the new persistent IDs.

1. Run `addactivity n/TestWalk date/TODAY start/09:00 end/10:00 r/once`, substituting the actual date.
   Repeat the command; expect different IDs and empty booking summaries. Add daily and weekly schedules
   with the same fields except for `r/daily` and `r/weekly`; expect each to be stored separately.
1. Try `date/2026-02-30`, `start/24:00`, an end equal to the start, an unknown recurrence, and a repeated field.
   Expect rejection without new schedules. Refer to the UG for the complete activity formats.
1. Run `editactivity ACTIVITY_ID n/TestWalkUpdated` on an empty schedule.
   Expect its ID and unspecified details to remain. Omit all edit fields or supply a nonexistent ID;
   expect an error without changing schedules.
1. Enrol the dog in that activity's `TODAY` occurrence. Try editing its name or time;
   expect rejection with advice to create a new activity. The booking and schedule remain unchanged.
1. Run `reset`. Expect the general card's `Dogs with upcoming bookings` label to contain the dog ID once.
   The Today card must include the dog booked today, even if a dog search hides its card.
1. Unenrol that booking, then enrol the dog in the daily schedule today and tomorrow.
   Expect its general card to list the dog ID once, and its Today card to show only today's booking.
1. Run `deleteactivity ACTIVITY_ID` using the daily schedule's ID. Expect the entire schedule and both
   dated bookings to disappear while dogs, customers, and other schedules remain. Repeat; expect an error.
1. To test historical records, use a disposable saved-data copy containing a valid past enrolment.
   Editing its activity must still fail; deleting the activity must remove that historical booking too.

### Searches, today, unlinked filters, and reset

Use a disposable dataset. Create two customers with distinct names and contacts and two dogs with different
breeds and sizes; link one dog to each customer. Also create one unowned dog and one customer without dogs.
Create a one-time activity today and a daily activity starting today with non-overlapping times, then enrol
the two owned dogs in different schedules today. Note all persistent IDs. Replace `TODAY` and the ID/name/size
placeholders below with these fixture values. Refer to the UG for complete search formats.

1. Run `searchd n/NAME s/SIZE` using the first dog's values. Expect matching dogs, their owners, and
   activities with their stored bookings. Run `searchd i/DOG_ID`; expect the exact dog regardless of prior
   visibility. Try an unsupported size and a nonexistent ID; expect errors and the previous display preserved.
1. Run `searchc i/CUSTOMER_ID`, then `searchc d/DOG_ID`, using an owned dog.
   Expect its owner and that owner's dogs, plus activities containing their bookings. Try both owned dog IDs
   in one `d/` field; neither customer should match because neither owns both. Try a nonexistent ID;
   expect rejection and no display change.
1. Run `searcha i/ACTIVITY_ID`, then `searcha d/TODAY t/09:00`, substituting a time inside a fixture's schedule.
   Expect all supplied criteria to apply together. Check `searcha r/daily` against the recurring fixture and
   search at its start and end times to verify inclusive matching. A dated search shows only dogs booked
   on that date and their owners. Add an empty schedule today; expect it to match `searcha d/TODAY` too.
1. Try `searcha i/0`, a nonexistent positive activity ID, an invalid date, and a repeated field.
   Expect errors with the previous display preserved. Repeat representative repeated/unsupported-prefix
   checks for `searchc` and `searchd`. Valid searches with no matches should empty the corresponding panels.
1. Book the unowned dog in the daily schedule tomorrow only. Run `today`.
   Expect schedules occurring today, only dogs booked today, and their owners; the unowned dog should be absent.
   Run `today extra`; expect rejection without changing the view.
1. Run `filterun dog`. Expect the unowned dog, no customers, and its booked daily schedule.
   Link that dog to an existing customer; it should leave the active filter. Run `filterun customer`;
   expect only customers with no dogs and empty dog/activity panels. Unlink a customer's last dog;
   that customer should enter this active filter. Try `filterun Dog`; expect rejection.
1. After each search, edit a matching name, change ownership, or change an enrolment as appropriate.
   Expect related panels to refresh while the filter stays active. In `searchd`, `searchc`, and `filterun dog`,
   check that activity enrolment details include only matching dogs and their stored dates.
1. Add a one-time schedule yesterday and a daily schedule starting yesterday. Run `reset`.
   Expect all customers and dogs, plus schedules with occurrences today or later. The past one-time schedule
   is hidden; the daily schedule remains visible. Run `reset extra`; expect rejection and no display change.

### Help and clear confirmation

1. Run `help adddog` and `help deletedog`. Expect guidance in the result area without executing either
   command or changing records or filters. Run `help summary today`; expect an error listing supported commands.
1. Run `help`, minimize its window, and run `help` again. Expect the existing window to restore and focus.
   Repeat via the Help menu and `F1`.
1. In a disposable dataset, add a dog, run `clear`, then enter `maybe`. Expect a Y/N prompt and no changes.
   Enter `reset` while confirmation is pending; expect another Y/N prompt rather than executing reset.
1. Enter ` n ` to cancel. Expect records and filters unchanged; `undo` should still undo the dog addition.
   Run `redo` to restore it.
1. Activate `searchc n/Alice`, run `clear`, and enter ` y `. Expect all customer, dog, activity, and enrolment
   records removed. Both `undo` and `redo` must report no available command. Add a customer named Bob with
   fresh valid contacts; expect success but no visible customer under the retained Alice filter.
   Run `reset` to see Bob. Restart and verify only the new records remain.

### Saving data

Use a disposable copy of the app folder for these tests.

1. Missing data file

   1. Ensure the app is closed, then remove `data/watchdog.json` and launch the app.<br>
      Expected: The app starts with sample data. A subsequent successful command recreates the data file.

1. Invalid JSON

   1. Ensure the app is closed, then replace the contents of `data/watchdog.json` with text that is not
      valid JSON and launch the app.<br>
      Expected: The app starts with an empty WatchDog data file and records a data-loading warning in the log.

1. Invalid persisted dog counter

   1. Save at least one dog, close the app, and change `nextDogId` in `data/watchdog.json` to zero,
      a non-integer value, or a value not greater than every stored dog ID. Relaunch the app.<br>
      Expected: The app rejects the inconsistent file, starts with an empty WatchDog data file, and records a
      data-loading warning. Restore the disposable file before continuing other tests.
