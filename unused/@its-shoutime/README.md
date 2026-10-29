<!--@@author its-shoutime-unused-->
# Unused code — @its-shoutime

The select-dog and select-customer implementation, tests, parsers, and UML diagrams
were moved here because these commands are not part of the final WatchDog product.

The files are retained for attribution and historical reference only. They are not
included in the Gradle source or test sets.

The archived Java files preserve their original repository paths from base
`39316339edadcf928b135d07858d62a2d06ae177`:

- `src/main/java/seedu/address/logic/commands/SelectDogCommand.java`
- `src/main/java/seedu/address/logic/commands/SelectCustomerCommand.java`
- `src/main/java/seedu/address/logic/parser/SelectCommandParser.java`
- `src/main/java/seedu/address/logic/parser/SelectDogCommandParser.java`
- `src/main/java/seedu/address/logic/parser/SelectCustomerCommandParser.java`
- The corresponding four command and parser test files under `src/test/java`.

Additional historical excerpts preserve the selection-only portions removed from
shared files:

- `excerpts/ModelManagerSelectionTests.java` preserves the five deleted selection tests
  from `src/test/java/seedu/address/model/ModelManagerTest.java`.
- `excerpts/ModelManagerSelectionMethods.java` preserves selection state and selection
  methods from `src/main/java/seedu/address/model/ModelManager.java`.
- `excerpts/ModelSelectionState.java` preserves the selection state record and interface
  declarations from `src/main/java/seedu/address/model/Model.java`.

These excerpts are historical attribution records and are excluded from the Gradle
source and test sets. The leftover selection fields, snapshot members, and methods
that remained in `Model` and `ModelManager` after the commands were retired were
removed from the active sources. The excerpts above are the retained copy.

`RecordListPanel` showed customers and dogs in one list. The application uses
`CustomerListPanel` and `DogListPanel` instead, so the combined panel and its
tests were moved here:

- `src/main/java/seedu/address/ui/RecordListPanel.java`
- `src/test/java/seedu/address/ui/RecordListPanelTest.java`
