# Retired code — Kenneth-Chia

These files preserve Kenneth-Chia's original category-search implementation from
base `39316339edadcf928b135d07858d62a2d06ae177`. The implementation was replaced
by the field-based `searchc` and `searchd` commands in PR #150 and is excluded
from the Gradle source and test sets.

The `SummaryCommandSelectionTest.java` file is an excerpt from
`src/test/java/seedu/address/logic/commands/SummaryCommandTest.java` at the same
base. It was retired with the select workflow and is preserved for authorship
reference only.

## Global search retirement

Global `search`, including its category compatibility routes, was retired from
`333692c5` on 27 October 2026 in favour of `searchc`, `searchd`, and `searcha`.
The five archived global command/parser/test files preserve that revision.
`SearchFieldParser` had no remaining callers. Excerpts preserve the orphaned
substring utility, its tests, and the global-search UI test. Replacement predicates
and both list commands remain active. Existing category archives are unchanged.

Git blame at that revision attributes 49/56 lines of SearchCommand, 24/26 of
SearchCommandParser, 41/41 of SearchFieldParser, 89/89 of SearchCommandTest, and
52/58 of SearchCommandParserTest to Kenneth. Xander's integration edits remain
intact. Class annotations follow the course's greater-than-80-percent rule and
do not imply every line was written by Kenneth. Git history retains provenance,
notably commits `0e2f9bd9`, `1a6b8e7b`, `de17000a`, and `e363bc9d`.

[Official authorship guidance](https://raw.githubusercontent.com/NUS-CS2103-AY2627-S1/website/master/admin/reposenseAuthorAnnotation.md)
requires unused functional and test code to be preserved with an explanation.
These archives remain outside Gradle source and test sets.
