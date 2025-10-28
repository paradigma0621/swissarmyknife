# SwissArmyKnife

**SwissArmyKnife** is a small desktop application that gathers several practical tools in one place.
It helps you perform everyday digital tasks such as copying passwords, comparing lists of numbers, or writing quick notes, all inside a single program.

---

## What It Is

SwissArmyKnife works like a “digital Swiss army knife”:
a main window loads several mini-applications (called *tools*).
Each tool opens in its own tab, so you can use them independently.

You can open the tools from the list on the left side of the window.
Some tools also respond to keyboard shortcuts for faster access.

---

## Included Tools

### 1. Clipboard Tool

This tool manages a password stored in a small text file.

* **Save a password:** Press **Ctrl + S** and enter a new password.
  It will be stored safely in the file `password.txt`.
* **Copy the password:** Press **Ctrl + P** to copy the saved password to the clipboard
  so it can be pasted elsewhere.

The file is located in your personal folder, usually
`~/swissarmyknife/password.txt`.

---

### 2. Comparator Tool

This tool compares two lists of numbers.

You can paste one list in the left box and another in the right box.
When you click **Compare** (or press **Ctrl + Enter**), the tool will show:

* Numbers that appear **only in the first list**
* Numbers that appear **only in the second list**
* Numbers that appear **in both lists**
* Numbers that are **repeated** in either list

This is useful for checking which numbers are missing, duplicated, or common between two data sets.

---

### 3. Scratch Tool

This is a simple notepad for short notes or ideas.

* Type any text in the main box.
* Press **Ctrl + S** to save it, or **Ctrl + L** to load the last saved text.

The notes are saved in a file named **`scratch.txt`**,
inside a folder defined by another file called **`pathConfiguration.txt`**.
That configuration file tells the application where to store your notes.
Example of content in `pathConfiguration.txt`:

```
/home/yourname/Documents/Notes
```

---

## How to Use

1. Start the program by opening the main SwissArmyKnife window.
   The list of tools appears on the left.
2. Double-click a tool name to open it in a tab.
3. Follow the on-screen instructions or use the shortcuts shown above.

No internet connection is required, and all files are stored locally on your computer.

---

## System Requirements

* Runs on **Windows**, **Linux**, or **macOS**.
* Requires **Java 25 or newer**.
* The program opens with a simple graphical interface (JavaFX).

You do not need programming knowledge to use it.

---

## Files Created

When you use the tools, the following files may appear in your personal folder:

| File                                     | Purpose                                                                         |
| ---------------------------------------- | ------------------------------------------------------------------------------- |
| `~/swissarmyknife/password.txt`          | Stores the password used by the Clipboard tool                                  |
| `~/swissarmyknife/pathConfiguration.txt` | Defines where the Scratch tool will save its notes                              |
| `scratch.txt`                            | The text saved by the Scratch tool (location defined in the configuration file) |

---

## License

This application is free.