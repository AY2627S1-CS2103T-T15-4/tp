---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

WatchDog stores its window size and position in `preferences.json` in the folder from which the application is launched.
These preferences are loaded at startup and saved when WatchDog closes.

The preferences file location is defined by `USER_PREFS_FILE_PATH` in `MainApp`.
The logging level is defined by `LOG_LEVEL` in `LogsCenter`; see the [Logging guide](Logging.md).
Changing either setting in the source requires rebuilding the application.
