---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

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

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

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

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
| `* * *`  | Dog day-care manager                                   | View a list of dogs currently present                                      | I can monitor the dogs at the day-care centre                     |
| `* * *`  | Dog day-care manager                                   | Record when a dog checks in and checks out                                 | I can accurately track which dogs are currently present           |
| `* * *`  | Dog day-care manager                                   | Mark scheduled activities as completed                                     | I can keep track of which tasks have been carried out             |
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

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  AddressBook shows a list of persons
3.  User requests to delete a specific person in the list
4.  AddressBook deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. AddressBook shows an error message.

      Use case resumes at step 2.

*{More to be added}*

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
* **Checklist**: A list of a dog's activities with an indication of which have been completed.
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
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
